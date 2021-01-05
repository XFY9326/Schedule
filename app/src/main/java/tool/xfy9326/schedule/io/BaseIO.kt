package tool.xfy9326.schedule.io

import io.ktor.utils.io.jvm.nio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object BaseIO {
    suspend fun File.deleteFile() = withContext(Dispatchers.IO) {
        when {
            isFile -> delete()
            isDirectory -> deleteRecursively()
            else -> true
        }
    }

    suspend fun prepareFileFolder(file: File) = withContext(Dispatchers.IO) {
        file.parentFile?.let {
            if (!it.exists()) return@withContext it.mkdirs()
        }
        return@withContext true
    }
}