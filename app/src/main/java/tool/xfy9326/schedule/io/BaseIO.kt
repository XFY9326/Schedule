package tool.xfy9326.schedule.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object BaseIO {
    suspend fun deleteFile(file: File) = withContext(Dispatchers.IO) {
        if (file.exists()) file.deleteRecursively() else true
    }

    suspend fun prepareFileFolder(file: File) = withContext(Dispatchers.IO) {
        file.parentFile?.let {
            if (!it.exists()) return@withContext it.mkdirs()
        }
        return@withContext true
    }
}