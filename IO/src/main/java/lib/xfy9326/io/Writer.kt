package lib.xfy9326.io

import kotlinx.coroutines.*
import lib.xfy9326.io.target.base.OutputTarget
import java.io.OutputStream
import kotlin.reflect.KClass

fun <T : Any, S : OutputStream> OutputTarget<S>.writeProcessing(dataClass: KClass<T>, block: suspend S.(data: T, dataClass: KClass<T>) -> Unit) =
    object : TargetWriter<T, S>(this@writeProcessing, dataClass) {
        override suspend fun onWrite(output: S, data: T, dataClass: KClass<T>) {
            output.block(data, dataClass)
        }
    }

@Suppress("BlockingMethodInNonBlockingContext")
abstract class TargetWriter<T : Any, S : OutputStream> internal constructor(private val target: OutputTarget<S>, private val dataClass: KClass<T>) {

    suspend fun write(data: T): Boolean =
        withContext(Dispatchers.IO + SupervisorJob()) {
            try {
                target.openOutputStream().use {
                    onWrite(it, data, dataClass)
                    it.flush()
                }
                return@withContext true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext false
        }

    protected abstract suspend fun onWrite(output: S, data: T, dataClass: KClass<T>)
}