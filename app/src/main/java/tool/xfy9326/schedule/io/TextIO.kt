package tool.xfy9326.schedule.io

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset

object TextIO {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun readAssetFileAsText(context: Context, path: String, defaultText: String? = null, charset: Charset = Charsets.UTF_8) =
        withContext(Dispatchers.IO) {
            try {
                return@withContext context.assets.open(path).bufferedReader(charset).readText()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext defaultText
        }

    suspend fun writeText(text: String, path: File, overwrite: Boolean = true, charset: Charset = Charsets.UTF_8) = withContext(Dispatchers.IO) {
        try {
            if (BaseIO.prepareFileFolder(path)) {
                if (overwrite) {
                    path.writeText(text, charset)
                } else {
                    path.appendText(text, charset)
                }
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    suspend fun readText(path: File, defaultText: String? = null, charset: Charset = Charsets.UTF_8) = withContext(Dispatchers.IO) {
        try {
            if (path.exists()) return@withContext path.readText(charset)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext defaultText
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun writeText(text: String, context: Context, outputUri: Uri, charset: Charset = Charsets.UTF_8) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(outputUri)?.bufferedWriter(charset)?.use {
                it.write(text)
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun readText(context: Context, outputUri: Uri, defaultText: String? = null, charset: Charset = Charsets.UTF_8) =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(outputUri)?.bufferedReader(charset)?.use {
                    return@withContext it.readText()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext defaultText
        }
}