package lib.xfy9326.io.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.io.File

fun File.asParentOf(path: String) = File(this, path)

suspend fun File.deleteAll() = withContext(Dispatchers.IO + SupervisorJob()) {
    when {
        isFile -> delete()
        isDirectory -> deleteRecursively()
        else -> true
    }
}

suspend fun File.createParentFolder() = withContext(Dispatchers.IO + SupervisorJob()) {
    val parent = parentFile
    if (parent?.exists() == true) parent.mkdirs() else true
}