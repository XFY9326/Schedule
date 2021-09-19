package tool.xfy9326.schedule.content

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lib.xfy9326.android.kit.io.kt.source
import lib.xfy9326.android.kit.io.kt.useBuffer
import lib.xfy9326.livedata.EventLiveData
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import okhttp3.internal.closeQuietly
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.base.ICourseImportConfig
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.js.JSCourseImportConfig.Companion.toCourseImportConfig
import tool.xfy9326.schedule.content.utils.BaseCourseImportConfig
import tool.xfy9326.schedule.content.utils.JSConfigException
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.make
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.report
import tool.xfy9326.schedule.io.JSFileManager
import kotlin.coroutines.CoroutineContext

class CourseImportConfigManager(scope: CoroutineScope) : CoroutineScope by scope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob() + CoroutineName(javaClass.simpleName)

    private val configLock = Mutex()
    private val currentImportConfigList = ArrayList<ICourseImportConfig>()
    private val importConfigs = MutableLiveData<List<ICourseImportConfig>>()
    private val preparedConfig = MutableEventLiveData<BaseCourseImportConfig>()
    private val prepareConfigProgress = MutableEventLiveData<Type>()
    private val operationError = MutableEventLiveData<JSConfigException>()
    private val operationAttention = MutableEventLiveData<Type>()
    private val configWarning = MutableEventLiveData<JSConfigException>()
    private val jsConfigExist = MutableEventLiveData<Pair<JSConfig, JSConfig>>()
    val courseImportConfigs: LiveData<List<ICourseImportConfig>>
        get() = importConfigs
    val preparedJSConfig: EventLiveData<BaseCourseImportConfig>
        get() = preparedConfig
    val jsConfigPrepareProgress: EventLiveData<Type>
        get() = prepareConfigProgress
    val configOperationError: EventLiveData<JSConfigException>
        get() = operationError
    val configOperationAttention: EventLiveData<Type>
        get() = operationAttention
    val configIgnorableWarning: EventLiveData<JSConfigException>
        get() = configWarning
    val jsConfigExistWarning: EventLiveData<Pair<JSConfig, JSConfig>> // Need import / Current exist
        get() = jsConfigExist

    private val httpClient = HttpClient(OkHttp) {
        install(HttpRedirect)
        BrowserUserAgent()
    }

    init {
        runConfigOperation {
            val configs = loadLocalConfigs()
            currentImportConfigList.clear()
            currentImportConfigList.addAll(configs)
            notifyListUpdate()
        }
    }

    fun addJSConfig(uri: Uri) = launch {
        try {
            val content = uri.source()?.useBuffer { readUtf8() } ?: error("Empty uri! Uri $uri")
            addJSConfig(JSFileManager.parserJSConfig(content), false)
        } catch (e: JSConfigException) {
            operationError.postEvent(e)
        } catch (e: Exception) {
            operationError.postEvent(JSConfigException.Error.READ_FAILED.make(e))
        }
    }

    fun addJSConfig(url: String) = launch {
        try {
            val content = httpClient.get<String>(url)
            addJSConfig(JSFileManager.parserJSConfig(content), false)
        } catch (e: JSConfigException) {
            operationError.postEvent(e)
        } catch (e: Exception) {
            operationError.postEvent(JSConfigException.Error.READ_FAILED.make(e))
        }
    }

    fun addJSConfig(jsConfig: JSConfig, force: Boolean) = runConfigOperation {
        val existConfig = currentImportConfigList.filterIsInstance<JSConfig>().find { it.id == jsConfig.id }
        if (!force && existConfig != null) {
            jsConfigExist.postEvent(jsConfig to existConfig)
            return@runConfigOperation
        }
        JSFileManager.addNewJSConfig(jsConfig)
        if (existConfig != null) {
            currentImportConfigList.remove(existConfig)
        }
        currentImportConfigList.add(jsConfig)
        notifyListUpdate()
        operationAttention.postEvent(Type.ADD_SUCCESS)
    }

    fun removeJSConfig(jsConfig: JSConfig) {
        runConfigOperation {
            if (jsConfig in currentImportConfigList) {
                JSFileManager.deleteJSConfigFiles(jsConfig.id, false)
                currentImportConfigList.remove(jsConfig)
                notifyListUpdate()
                operationAttention.postEvent(Type.REMOVE_SUCCESS)
            } else {
                operationError.postEvent(JSConfigException.Error.CONFIG_DELETE_ERROR.make())
            }
        }
    }

    fun prepareJSConfig(jsConfig: JSConfig): Job = runConfigOperation {
        if (jsConfig.updateUrl != null) prepareConfigProgress.postEvent(Type.CHECK_UPDATE)
        val latestConfig = try {
            getLatestConfig(jsConfig)
        } catch (e: JSConfigException) {
            configWarning.postEvent(e)
            jsConfig
        }
        if (!JSFileManager.checkLocalJSConfigFiles(latestConfig)) {
            JSFileManager.deleteJSConfigFiles(latestConfig.id, true)
            prepareConfigProgress.postEvent(Type.PREPARE_PROVIDER)
            downloadJS(latestConfig.id, latestConfig.providerJSUrl, JSConfigException.Error.PROVIDER_DOWNLOAD_ERROR, JSFileManager.SaveType.PROVIDER)
            prepareConfigProgress.postEvent(Type.PREPARE_PARSER)
            downloadJS(latestConfig.id, latestConfig.parserJSUrl, JSConfigException.Error.PARSER_DOWNLOAD_ERROR, JSFileManager.SaveType.PARSER)
            prepareConfigProgress.postEvent(Type.PREPARE_DEPENDENCIES)
            for (dependenciesJSUrl in latestConfig.dependenciesJSUrls) {
                downloadJS(latestConfig.id, dependenciesJSUrl, JSConfigException.Error.DEPENDENCIES_DOWNLOAD_ERROR, JSFileManager.SaveType.DEPENDENCY)
            }
            if (!JSFileManager.checkLocalJSConfigFiles(latestConfig)) {
                JSConfigException.Error.PREPARE_ERROR.report()
            }
        }
        prepareConfigProgress.postEvent(Type.PREPARE_FINISH)
        preparedConfig.postEvent(latestConfig.toCourseImportConfig())
    }

    private suspend fun downloadJS(uuid: String, url: String, errorType: JSConfigException.Error, saveType: JSFileManager.SaveType) =
        JSFileManager.downloadJS(httpClient, uuid, url, errorType, saveType)

    private suspend fun getLatestConfig(jsConfig: JSConfig): JSConfig {
        if (jsConfig.updateUrl == null) {
            return jsConfig
        } else {
            try {
                val content = httpClient.get<String>(jsConfig.updateUrl)
                val config = JSFileManager.parserJSConfig(content)
                return when {
                    config == jsConfig -> jsConfig
                    config.id != jsConfig.id -> error("JSConfig UUID changed! Old: ${jsConfig.id}  New: ${config.id}")
                    else -> {
                        JSFileManager.addNewJSConfig(config)
                        currentImportConfigList.remove(jsConfig)
                        currentImportConfigList.add(config)
                        notifyListUpdate()
                        config
                    }
                }
            } catch (e: JSConfigException) {
                if (e.cause != null) {
                    JSConfigException.Error.UPDATE_FAILED.report(e.cause)
                } else {
                    JSConfigException.Error.UPDATE_FAILED.report()
                }
            } catch (e: Exception) {
                JSConfigException.Error.UPDATE_FAILED.report(e)
            }
        }
    }

    private fun notifyListUpdate() = importConfigs.postValue(ArrayList(currentImportConfigList).sortedBy { it.lowerCaseSortingBasis })

    private suspend fun loadLocalConfigs(): List<ICourseImportConfig> {
        val localConfigs = CourseImportConfigRegistry.getConfigs()
        val jsConfigs = JSFileManager.loadJSConfigs()
        return if (jsConfigs != null) {
            localConfigs + jsConfigs
        } else {
            localConfigs
        }.sortedBy { it.lowerCaseSortingBasis }
    }

    private fun runConfigOperation(block: suspend CoroutineScope.() -> Unit) = launch {
        configLock.withLock {
            try {
                block(this)
            } catch (e: CancellationException) {
                // Ignore
            } catch (e: JSConfigException) {
                operationError.postEvent(e)
            } catch (e: Exception) {
                operationError.postEvent(JSConfigException.Error.UNKNOWN_ERROR.make(e))
            }
        }
    }

    fun close() {
        httpClient.cancel()
        httpClient.closeQuietly()
        cancel()
    }

    enum class Type {
        ADD_SUCCESS,
        REMOVE_SUCCESS,

        CHECK_UPDATE,
        PREPARE_PROVIDER,
        PREPARE_PARSER,
        PREPARE_DEPENDENCIES,
        PREPARE_FINISH;

        companion object {
            fun Type.getText(context: Context) =
                context.getString(
                    when (this) {
                        ADD_SUCCESS -> R.string.js_config_config_add_success
                        REMOVE_SUCCESS -> R.string.js_config_config_remove_success
                        CHECK_UPDATE -> R.string.js_config_check_update
                        PREPARE_PROVIDER -> R.string.js_config_prepare_provider
                        PREPARE_PARSER -> R.string.js_config_prepare_parser
                        PREPARE_DEPENDENCIES -> R.string.js_config_prepare_dependencies
                        PREPARE_FINISH -> R.string.js_config_prepare_finish
                    }
                )
        }
    }
}