@file:Suppress("unused")

package tool.xfy9326.schedule.tools.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

fun <T> suspendLazyValue(coroutineContext: CoroutineContext = Dispatchers.Default, initializer: suspend () -> T): SuspendLazyValue<T> =
    SuspendLazyValueImpl(coroutineContext, initializer)

interface SuspendLazyValue<T> {
    suspend fun value(): T

    suspend fun refresh(): T

    suspend fun preload()
}

private class ValueWrapper<T>(val value: T)

private class SuspendLazyValueImpl<T>(
    private val coroutineContext: CoroutineContext,
    private val initializer: suspend () -> T
) : SuspendLazyValue<T> {
    private val mutex = Mutex()
    private var cached: ValueWrapper<T>? = null

    override suspend fun value(): T =
        cached?.value ?: mutex.withLock {
            cached?.value ?: withContext(coroutineContext) {
                initializer()
            }.also {
                cached = ValueWrapper(it)
            }
        }

    override suspend fun refresh(): T =
        mutex.withLock {
            withContext(coroutineContext) {
                initializer()
            }.also {
                cached = ValueWrapper(it)
            }
        }

    override suspend fun preload() {
        CoroutineScope(coroutineContext).launch {
            value()
        }
    }
}