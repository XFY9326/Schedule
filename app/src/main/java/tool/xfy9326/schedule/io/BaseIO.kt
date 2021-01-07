package tool.xfy9326.schedule.io

import android.content.Context
import android.net.Uri
import io.ktor.utils.io.jvm.nio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Suppress("BlockingMethodInNonBlockingContext")
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

    suspend fun writeFileToUri(context: Context, uri: Uri, localFile: File) = withContext(Dispatchers.IO) {
        if (localFile.isFile) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    localFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                return@withContext true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        false
    }
}