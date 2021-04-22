package tool.xfy9326.schedule.content

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lib.xfy9326.livedata.EventLiveData
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig.Companion.toCourseImportConfig
import tool.xfy9326.schedule.content.base.ICourseImportConfig
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.utils.JSConfigException
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.make
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.report
import tool.xfy9326.schedule.io.JSFileManager
import tool.xfy9326.schedule.io.kt.source
import tool.xfy9326.schedule.io.kt.useBuffer
import kotlin.coroutines.CoroutineContext

class CourseImportConfigManager(scope: CoroutineScope) : CoroutineScope by scope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob() + CoroutineName(CourseImportConfigManager::class.simpleName.orEmpty())

    private val configLock = Mutex()
    private val currentImportConfigList = ArrayList<ICourseImportConfig>()
    private val importConfigs = MutableLiveData<List<ICourseImportConfig>>()
    private val preparedConfig = MutableEventLiveData<AbstractCourseImportConfig<*, *, *, *>>()
    private val prepareConfigProgress = MutableEventLiveData<Type>()
    private val operationError = MutableEventLiveData<JSConfigException>()
    private val operationAttention = MutableEventLiveData<Type>()
    val courseImportConfigs: LiveData<List<ICourseImportConfig>>
        get() = importConfigs
    val preparedJSConfig: EventLiveData<AbstractCourseImportConfig<*, *, *, *>>
        get() = preparedConfig
    val jsConfigPrepareProgress: EventLiveData<Type>
        get() = prepareConfigProgress
    val configOperationError: EventLiveData<JSConfigException>
        get() = operationError
    val configOperationAttention: EventLiveData<Type>
        get() = operationAttention

    private val httpClient = HttpClient(OkHttp) {
        install(HttpCookies)
        install(HttpRedirect)
        BrowserUserAgent()
        engine {
            config {
                followRedirects(true)
                followSslRedirects(true)
                retryOnConnectionFailure(true)
            }
        }
    }

    init {
        runConfigOperation {
            val configs = loadLocalConfigs()
            currentImportConfigList.clear()
            currentImportConfigList.addAll(configs)
            notifyListUpdate()
        }
    }

    fun addJSConfig(uri: Uri) {
        launch {
            try {
                val content = uri.source()?.useBuffer { readUtf8() } ?: error("Empty uri! Uri $uri")
                addJSConfigContent(content)
            } catch (e: Exception) {
                operationError.postEvent(JSConfigException.Error.READ_FAILED.make(e))
            }
        }
    }

    fun addJSConfig(url: String) {
        launch {
            try {
                val content = httpClient.get<String>(url)
                addJSConfigContent(content)
            } catch (e: Exception) {
                operationError.postEvent(JSConfigException.Error.READ_FAILED.make(e))
            }
        }
    }

    private fun addJSConfigContent(content: String) {
        runConfigOperation {
            val jsConfig = JSFileManager.parserJSConfig(content)
            JSFileManager.addNewJSConfig(jsConfig)
            val existConfig = currentImportConfigList.filterIsInstance<JSConfig>().find { it.uuid == jsConfig.uuid }
            if (existConfig != null) {
                currentImportConfigList.remove(existConfig)
            }
            currentImportConfigList.add(jsConfig)
            notifyListUpdate()
            operationAttention.postEvent(Type.ADD_SUCCESS)
        }
    }

    fun removeJSConfig(jsConfig: JSConfig) {
        runConfigOperation {
            if (jsConfig in currentImportConfigList) {
                JSFileManager.deleteJSConfigFiles(jsConfig.uuid, false)
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
        val latestConfig = getLatestConfig(jsConfig)
        if (!JSFileManager.checkLocalJSConfigFiles(latestConfig)) {
            JSFileManager.deleteJSConfigFiles(latestConfig.uuid, true)
            prepareConfigProgress.postEvent(Type.PREPARE_PROVIDER)
            downloadJS(latestConfig.providerJSUrl, JSConfigException.Error.PROVIDER_DOWNLOAD_ERROR) {
                JSFileManager.writeJSProvider(latestConfig.uuid, it)
            }
            prepareConfigProgress.postEvent(Type.PREPARE_PARSER)
            downloadJS(latestConfig.parserJSUrl, JSConfigException.Error.PARSER_DOWNLOAD_ERROR) {
                JSFileManager.writeJSParser(latestConfig.uuid, it)
            }
            prepareConfigProgress.postEvent(Type.PREPARE_DEPENDENCIES)
            for (dependenciesJSUrl in latestConfig.dependenciesJSUrls) {
                downloadJS(dependenciesJSUrl, JSConfigException.Error.DEPENDENCIES_DOWNLOAD_ERROR) {
                    JSFileManager.writeJSDependencies(latestConfig.uuid, dependenciesJSUrl, it)
                }
            }
            if (!JSFileManager.checkLocalJSConfigFiles(latestConfig)) {
                JSConfigException.Error.PREPARE_ERROR.report()
            }
        }
        preparedConfig.postEvent(latestConfig.toCourseImportConfig())
    }

    private suspend fun downloadJS(url: String, errorType: JSConfigException.Error, onSave: suspend (String) -> Boolean) {
        try {
            val content = httpClient.get<String>(url)
            if (content.isBlank()) {
                error("JS content empty!")
            } else {
                if (!onSave(content)) {
                    errorType.make()
                }
            }
        } catch (e: JSConfigException) {
            throw e
        } catch (e: Exception) {
            errorType.report(e)
        }
    }

    private suspend fun getLatestConfig(jsConfig: JSConfig): JSConfig {
        if (jsConfig.updateUrl == null) {
            return jsConfig
        } else {
            try {
                val content = httpClient.get<String>(jsConfig.updateUrl)
                val config = JSFileManager.parserJSConfig(content)
                return when {
                    config == jsConfig -> jsConfig
                    config.uuid != jsConfig.uuid -> error("JSConfig UUID changed! Old: ${jsConfig.uuid}  New: ${config.uuid}")
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

    enum class Type(@StringRes val msgId: Int) {
        ADD_SUCCESS(R.string.js_config_config_add_success),
        REMOVE_SUCCESS(R.string.js_config_config_remove_success),
        CHECK_UPDATE(R.string.js_config_check_update),
        PREPARE_PROVIDER(R.string.js_config_prepare_provider),
        PREPARE_PARSER(R.string.js_config_prepare_parser),
        PREPARE_DEPENDENCIES(R.string.js_config_prepare_dependencies);

        fun getText(context: Context) = context.getString(msgId)
    }
}