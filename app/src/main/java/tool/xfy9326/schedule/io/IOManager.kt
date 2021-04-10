@file:Suppress("unused")

package tool.xfy9326.schedule.io

import android.content.ContentResolver
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.io.kt.deleteAll
import tool.xfy9326.schedule.io.kt.runSimpleIOJob
import tool.xfy9326.schedule.io.utils.DirUtils
import java.io.File

object IOManager {
    private val appContext by lazy { App.instance }
    private const val FILE_PROVIDER_AUTH = BuildConfig.APPLICATION_ID + ".file.provider"

    val contentResolver: ContentResolver by lazy { appContext.contentResolver }
    val assetManager: AssetManager by lazy { appContext.assets }
    val resources: Resources by lazy { appContext.resources }

    fun requestScanMediaFile(uri: Uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            appContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
        }
    }

    fun File.getUriByFileProvider(): Uri? =
        try {
            FileProvider.getUriForFile(appContext, FILE_PROVIDER_AUTH, this)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }

    suspend fun clearAllCache() {
        runSimpleIOJob {
            DirUtils.cacheDir.deleteAll()
            DirUtils.codeCacheDir.deleteAll()
            DirUtils.externalCacheDirs.deleteAll()
        }
    }
}