package lib.xfy9326.io

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import lib.xfy9326.io.target.base.InputTarget
import java.io.InputStream
import kotlin.reflect.KClass

fun <T : Any, S : InputStream> InputTarget<S>.readProcessing(dataClass: KClass<T>, block: suspend S.(dataClass: KClass<T>) -> T) =
    object : TargetReader<T, S>(this@readProcessing, dataClass) {
        override suspend fun onRead(input: S, dataClass: KClass<T>): T = input.block(dataClass)
    }

abstract class TargetReader<T : Any, S : InputStream> internal constructor(private val target: InputTarget<S>, private val dataClass: KClass<T>) {

    fun asFlow() = flow {
        target.openInputStream().use {
            emit(onRead(it, dataClass))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun read(): T? =
        withContext(Dispatchers.IO + SupervisorJob()) {
            try {
                return@withContext target.openInputStream().use {
                    onRead(it, dataClass)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext null
        }

    protected abstract suspend fun onRead(input: S, dataClass: KClass<T>): T
}