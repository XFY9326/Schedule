package tool.xfy9326.schedule.io

import android.content.ContentResolver
import android.content.res.AssetManager
import android.content.res.Resources
import androidx.core.content.ContextCompat
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.io.BaseIO.deleteFile

object GlobalIO {
    private val assetManager: AssetManager by lazy { App.instance.assets }
    val contentResolver: ContentResolver by lazy { App.instance.contentResolver }
    val resources: Resources by lazy { App.instance.resources }

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