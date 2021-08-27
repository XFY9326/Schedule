@file:Suppress("unused")

package lib.xfy9326.kit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

private suspend fun <T> runIOJob(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.IO + SupervisorJob(), block)

suspend fun <T> runUnsafeIOJob(block: suspend CoroutineScope.() -> T) = runIOJob { runCatching { block() }.getOrThrow() }

suspend fun <T> runSafeIOJob(block: suspend CoroutineScope.() -> T) =
    runIOJob { runCatching { block() }.onFailure { it.printStackTrace() }.getOrNull() }

suspend fun <T> runSafeIOJob(defaultValue: T, block: suspend CoroutineScope.() -> T) =
    runIOJob { runCatching { block() }.onFailure { it.printStackTrace() }.getOrDefault(defaultValue) }

suspend fun runSimpleIOJob(block: suspend CoroutineScope.() -> Unit): Unit =
    runIOJob { runCatching { block() }.onFailure { it.printStackTrace() } }

suspend fun runOnlyResultIOJob(block: suspend CoroutineScope.() -> Boolean) = runSafeIOJob(false, block)