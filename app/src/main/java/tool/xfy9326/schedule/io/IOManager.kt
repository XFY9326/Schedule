package tool.xfy9326.schedule.io

import android.content.ContentResolver
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import lib.xfy9326.android.kit.AppDir
import lib.xfy9326.android.kit.ApplicationInstance
import lib.xfy9326.kit.deleteRecursively
import lib.xfy9326.kit.runSimpleIOJob
import tool.xfy9326.schedule.BuildConfig
import java.io.File

object IOManager {
    private val appContext by lazy { ApplicationInstance }
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
            AppDir.cacheDir.deleteRecursively()
            AppDir.codeCacheDir.deleteRecursively()
            AppDir.externalCacheDirs.deleteRecursively()
        }
    }
}