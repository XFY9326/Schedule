package tool.xfy9326.schedule.io

import android.net.Uri
import io.ktor.util.cio.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.kt.readText
import tool.xfy9326.schedule.kt.writeText
import java.io.File
import java.nio.charset.Charset
import kotlin.io.use

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

    suspend fun writeText(text: String, file: File, overwrite: Boolean = true, charset: Charset = Charsets.UTF_8) = withContext(Dispatchers.IO) {
        try {
            if (BaseIO.prepareFileFolder(file)) {
                if (overwrite) {
                    file.writeText(text, charset)
                } else {
                    file.appendText(text, charset)
                }
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    suspend fun readText(file: File, defaultText: String? = null, charset: Charset = Charsets.UTF_8) = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) return@withContext file.readText(charset)
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

    suspend fun readText(outputUri: Uri, defaultText: String? = null, charset: Charset = Charsets.UTF_8) = withContext(Dispatchers.IO) {
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