package lib.xfy9326.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import lib.xfy9326.io.target.base.InputTarget
import lib.xfy9326.io.target.base.OutputTarget
import java.io.InputStream
import java.io.OutputStream

fun InputTarget<out InputStream>.copyTo(outputTarget: OutputTarget<out OutputStream>) = TargetPipe(this, outputTarget)

@Suppress("BlockingMethodInNonBlockingContext")
class TargetPipe internal constructor(private val inputTarget: InputTarget<out InputStream>, private val outputTarget: OutputTarget<out OutputStream>) {

    suspend fun start(bufferSize: Int = DEFAULT_BUFFER_SIZE) =
        withContext(Dispatchers.IO + SupervisorJob()) {
            try {
                inputTarget.openInputStream().use { input ->
                    outputTarget.openOutputStream().use { output ->
                        return@withContext input.copyTo(output, bufferSize).also {
                            output.flush()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext null
        }
}