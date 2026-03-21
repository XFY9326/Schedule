package tool.xfy9326.schedule.tools.base

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.Serializable

fun <T : Serializable> T.deepClone(): Result<T> =
    runCatching {
        PipedOutputStream().use { output ->
            PipedInputStream(output).use { input ->
                ObjectOutputStream(output).use { objectOutput ->
                    ObjectInputStream(input).use { objectInput ->
                        objectOutput.writeObject(this@deepClone)
                        objectOutput.flush()
                        objectInput.readObject().cast()
                    }
                }
            }
        }
    }
