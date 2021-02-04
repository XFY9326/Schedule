package tool.xfy9326.schedule.io

import android.content.ContentResolver
import android.content.res.AssetManager
import android.content.res.Resources
import android.net.Uri
import androidx.core.content.ContextCompat
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.io.BaseIO.deleteFile
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@Suppress("MemberVisibilityCanBePrivate", "unused")
object GlobalIO {
    private val assetManager: AssetManager by lazy { App.instance.assets }
    val contentResolver: ContentResolver by lazy { App.instance.contentResolver }
    val resources: Resources by lazy { App.instance.resources }

    fun Uri.asInputStream() = contentResolver.openInputStream(this)

    fun Uri.asOutputStream() = contentResolver.openOutputStream(this)

    fun Uri.writeText(text: String, charset: Charset = StandardCharsets.UTF_8) = writeBytes(text.toByteArray(charset))

    fun Uri.writeBytes(byteArray: ByteArray) = asOutputStream()?.use { it.write(byteArray) } != null

    fun Uri.readText(charset: Charset = StandardCharsets.UTF_8) = asInputStream()?.reader(charset)?.use { it.readText() }

    fun Uri.readBytes() = asInputStream()?.use { it.readBytes() }

    fun openAsset(path: String) = assetManager.open(path)

    suspend fun clearAllCache() {
        App.instance.apply {
            cacheDir?.deleteFile()
            ContextCompat.getCodeCacheDir(this)?.deleteFile()
            ContextCompat.getExternalCacheDirs(this).forEach {
                it?.deleteFile()
            }
        }
    }
}