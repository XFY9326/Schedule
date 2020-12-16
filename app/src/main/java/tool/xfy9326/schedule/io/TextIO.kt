package tool.xfy9326.schedule.io

import android.content.Context
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
}