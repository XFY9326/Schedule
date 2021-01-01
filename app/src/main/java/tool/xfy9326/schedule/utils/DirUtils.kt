package tool.xfy9326.schedule.utils

import android.os.Environment
import androidx.core.content.ContextCompat
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.kt.asParentOf
import java.io.File

object DirUtils {
    private const val DIR_EXTERNAL = "External"
    private const val DIR_CACHE = "Cache"
    private const val DIR_SHARE = "Share"
    private const val DIR_LOG = "Log"
    private const val DIR_PICTURE_APP = "App"
    const val DIR_SCHEDULE = "Schedule"

    const val ASSETS_LICENSE_FILE = "LICENSE.txt"
    const val ASSETS_EULA_FILE = "EULA.txt"

    val LogDir
        get() = getExternalFilesDir(DIR_LOG)

    val PictureAppDir
        get() = getExternalFilesDir(Environment.DIRECTORY_PICTURES, DIR_PICTURE_APP)

    private val ExternalCacheDir
        get() = getSafeExternalDir(ContextCompat.getExternalCacheDirs(App.instance).filterNotNull(), DIR_CACHE)

    val ShareDir
        get() = ExternalCacheDir.asParentOf(DIR_SHARE)

    private fun getInternalFilesDir(vararg subDir: String): File =
        if (subDir.isEmpty()) {
            App.instance.filesDir
        } else {
            File(App.instance.filesDir, subDir.joinToString(File.separator))
        }

    private fun getExternalFilesDir(vararg subDir: String): File {
        val joinedSubDir = subDir.joinToString(File.separator)
        val dirList = ContextCompat.getExternalFilesDirs(App.instance, joinedSubDir).filterNotNull()
        return getSafeExternalDir(dirList, joinedSubDir)
    }

    private fun getSafeExternalDir(dirList: List<File>, subDir: String) =
        if (dirList.isEmpty()) {
            getInternalFilesDir(DIR_EXTERNAL, subDir)
        } else {
            dirList.first()
        }
}