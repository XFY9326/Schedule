package tool.xfy9326.schedule.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import io.github.xfy9326.atools.coroutines.withTryLock
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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
import java.net.ConnectException

object UpgradeUtils {
    private const val CURRENT_VERSION = BuildConfig.VERSION_CODE
    private const val IS_BETA = BuildConfig.IS_BETA

    private const val UPDATE_PRODUCT = "Schedule"
    private const val UPDATE_LATEST = "Latest"
    private const val UPDATE_INDEX = "Index"

    private val UPDATE_SERVERS = arrayOf(
        "https://update.xfy9326.top",
        "https://xfy9326.gitee.io/updates",
        "https://xfy9326.github.io/Updates",
        "https://gitee.com/XFY9326/updates/raw/master",
        "https://raw.githubusercontent.com/XFY9326/Updates/master"
    )

    private val UPDATE_CHECK_MUTEX = Mutex()

    private fun getLatestCheckUrl(server: String): String =
        "$server/$UPDATE_PRODUCT/$UPDATE_LATEST"

    private fun getIndexUrl(server: String): String =
        "$server/$UPDATE_PRODUCT/$UPDATE_INDEX"

    fun checkUpgrade(
        lifecycleOwner: LifecycleOwner,
        forcedCheck: Boolean,
        onFailed: (() -> Unit)? = null,
        onNoUpgrade: (() -> Unit)? = null,
        onFoundUpgrade: ((UpdateInfo) -> Unit)? = null,
    ) {
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
        if (forcedCheck || !IS_BETA && latest.versionCode > AppDataStore.ignoreUpdateVersionCodeFlow.first()) return false
        return true
    }

    private suspend fun getUpdateData(currentVersionCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        getDefaultClient().use {
            val latest = getLatestVersion(it)
            if (latest.versionCode > currentVersionCode || IS_BETA && latest.versionCode == currentVersionCode) {
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
        UPDATE_SERVERS.map { getLatestCheckUrl(it) }.forEach {
            runCatching {
                client.get(it).body<UpdateInfo>()
            }.onSuccess {
                return it
            }.onFailure {
                if (it !is ConnectTimeoutException) throw it
            }
        }
        throw ConnectException("Unable to connect any server!")
    }

    private suspend fun getIndex(client: HttpClient): List<UpdateIndex> {
        UPDATE_SERVERS.map { getIndexUrl(it) }.forEach {
            runCatching {
                client.get(it).body<List<UpdateIndex>>()
            }.onSuccess {
                return it
            }.onFailure {
                if (it !is ConnectTimeoutException) throw it
            }
        }
        throw ConnectException("Unable to connect any server!")
    }

    private fun getDefaultClient() = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true }, contentType = ContentType.Any)
        }
        install(HttpRedirect)
    }
}