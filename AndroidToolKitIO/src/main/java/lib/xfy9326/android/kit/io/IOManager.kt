package lib.xfy9326.android.kit.io

import android.content.ContentResolver
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import lib.xfy9326.android.kit.AppDir
import lib.xfy9326.android.kit.ApplicationInstance
import lib.xfy9326.kit.deleteRecursively
import lib.xfy9326.kit.runSimpleIOJob

object IOManager {
    private val appContext by lazy { ApplicationInstance }

    val contentResolver: ContentResolver by lazy { appContext.contentResolver }
    val assetManager: AssetManager by lazy { appContext.assets }
    val resources: Resources by lazy { appContext.resources }

    fun requestScanMediaFile(uri: Uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            appContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
        }
    }

    suspend fun clearAllCache() {
        runSimpleIOJob {
            AppDir.cacheDir.deleteRecursively()
            AppDir.codeCacheDir.deleteRecursively()
            AppDir.externalCacheDirs.deleteRecursively()
        }
    }
}