package tool.xfy9326.schedule.io

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.io.GlobalIO.readText
import tool.xfy9326.schedule.io.GlobalIO.writeText
import java.io.File
import java.nio.charset.Charset

object TextIO {

    suspend fun readAssetText(path: String, defaultText: String? = null, charset: Charset = Charsets.UTF_8) =
        withContext(Dispatchers.IO) {
            try {
                return@withContext GlobalIO.openAsset(path).reader(charset).use { it.readText() }
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

    suspend fun writeText(text: String, outputUri: Uri, charset: Charset = Charsets.UTF_8) = withContext(Dispatchers.IO) {
        try {
            return@withContext outputUri.writeText(text, charset)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    suspend fun readText(outputUri: Uri, defaultText: String? = null, charset: Charset = Charsets.UTF_8) =
        withContext(Dispatchers.IO) {
            try {
                outputUri.readText(charset)?.let {
                    return@withContext it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext defaultText
        }
}