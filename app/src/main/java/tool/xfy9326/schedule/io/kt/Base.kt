@file:Suppress("unused")

package tool.xfy9326.schedule.io.kt

import androidx.annotation.RawRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.io.file.AssetFile
import tool.xfy9326.schedule.io.file.RawResFile

fun assetFile(path: String) = AssetFile(path)

fun rawResFile(@RawRes resId: Int) = RawResFile(resId)

private suspend fun <T> runIOJob(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.IO + SupervisorJob(), block)

suspend fun <T> runUnsafeIOJob(block: suspend CoroutineScope.() -> T) =
    runIOJob { runCatching { block() }.getOrThrow() }

suspend fun <T> runSafeIOJob(block: suspend CoroutineScope.() -> T) =
    runIOJob { runCatching { block() }.onFailure { it.printStackTrace() }.getOrNull() }

suspend fun <T> runSafeIOJob(defaultValue: T, block: suspend CoroutineScope.() -> T) =
    runIOJob { runCatching { block() }.onFailure { it.printStackTrace() }.getOrDefault(defaultValue) }

suspend fun runOnlyResultIOJob(block: suspend CoroutineScope.() -> Boolean) = runSafeIOJob(false, block)