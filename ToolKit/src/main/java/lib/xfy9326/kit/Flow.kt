@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package lib.xfy9326.kit

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch


fun <T1, T2> Flow<T1>.combine(transform: suspend (T1) -> Flow<T2>): Flow<T2> =
    combineTransform(transform) { _, t2 ->
        t2
    }

fun <T1, T2, R> Flow<T1>.combineTransform(combineTransform: suspend (T1) -> Flow<T2>, transform: suspend (T1, T2) -> R): Flow<R> =
    channelFlow {
        var oldJob: Job? = null
        collect { t1 ->
            oldJob?.cancel()
            oldJob = launch {
                combineTransform(t1).collect { t2 ->
                    send(transform(t1, t2))
                }
            }
        }
        awaitClose {
            oldJob?.cancel()
        }
    }