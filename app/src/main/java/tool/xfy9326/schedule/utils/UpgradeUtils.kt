package tool.xfy9326.schedule.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.json.upgrade.UpdateIndex
import tool.xfy9326.schedule.json.upgrade.UpdateInfo
import tool.xfy9326.schedule.tools.base.EMPTY
import tool.xfy9326.schedule.tools.coroutines.withTryLock

object UpgradeUtils {
    private const val CURRENT_VERSION = BuildConfig.VERSION_CODE

    private val UPDATE_CHECK_MUTEX = Mutex()

    private fun getLatestCheckUrl(): String = EMPTY

    private fun getIndexUrl(): String = EMPTY

    fun checkUpgrade(
        lifecycleOwner: LifecycleOwner,
        forcedCheck: Boolean,
        onFailed: (() -> Unit)? = null,
        onNoUpgrade: (() -> Unit)? = null,
        onFoundUpgrade: ((UpdateInfo) -> Unit)? = null,
    ) {
        @Suppress("KotlinConstantConditions")
        if (!BuildConfig.ENABLE_UPDATE_CHECK) {
            onNoUpgrade?.invoke()
            return
        }

        lifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            UPDATE_CHECK_MUTEX.withTryLock {
                requestUpgrade(forcedCheck).let {
                    if (it.first) {
                        val info = it.second
                        if (info == null) {
                            if (onNoUpgrade != null) lifecycleOwner.withStarted { onNoUpgrade() }
                        } else {
                            if (onFoundUpgrade != null) lifecycleOwner.withStarted { onFoundUpgrade(info) }
                        }
                    } else {
                        if (onFailed != null) lifecycleOwner.withStarted { onFailed() }
                    }
                }
            }
        }
    }

    private suspend fun requestUpgrade(forcedCheck: Boolean): Pair<Boolean, UpdateInfo?> {
        try {
            getUpdateData(CURRENT_VERSION)?.let {
                if (!validateIgnoreUpdate(forcedCheck, it)) {
                    return true to it
                }
            }
            return true to null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false to null
    }

    private suspend fun validateIgnoreUpdate(forcedCheck: Boolean, latest: UpdateInfo): Boolean {
        if (latest.forceUpdate) return false
        if (forcedCheck || latest.versionCode > AppDataStore.ignoreUpdateVersionCodeFlow.first()) return false
        return true
    }

    private suspend fun getUpdateData(currentVersionCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        getDefaultClient().use {
            val latest = getLatestVersion(it)
            if (latest.versionCode > currentVersionCode) {
                val index = getIndex(it)
                val forceUpdate = validateForceUpdate(currentVersionCode, latest, index)
                return@withContext latest.copy(forceUpdate = forceUpdate)
            }
        }
        return@withContext null
    }

    private fun validateForceUpdate(currentVersionCode: Int, latest: UpdateInfo, index: List<UpdateIndex>): Boolean {
        if (latest.forceUpdate) return true
        val sortedIndex = index.sortedByDescending { it.version }
        if (sortedIndex.last().version > currentVersionCode) return true
        sortedIndex.find { it.version > currentVersionCode && it.forceUpdate }?.let {
            return true
        }
        return false
    }

    private suspend fun getLatestVersion(client: HttpClient): UpdateInfo {
        return client.get(getLatestCheckUrl()).body<UpdateInfo>()
    }

    private suspend fun getIndex(client: HttpClient): List<UpdateIndex> {
        return client.get(getIndexUrl()).body<List<UpdateIndex>>()
    }

    private fun getDefaultClient() = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true }, contentType = ContentType.Any)
        }
        install(HttpRedirect)
    }
}