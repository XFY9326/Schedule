@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package tool.xfy9326.schedule.io.utils

import androidx.core.content.ContextCompat
import tool.xfy9326.schedule.kt.AppInstance
import java.io.File

object DirUtils {
    private val appContext by lazy { AppInstance }

    fun externalFilesDirs(vararg childDirs: String): List<File> =
        ContextCompat.getExternalFilesDirs(appContext, if (childDirs.isEmpty()) null else childDirs.joinToString(File.separator)).filterNotNull()

    fun externalFilesDir(vararg childDirs: String): File = externalFilesDirs(*childDirs).firstOrNull() ?: error("No external files dir!")

    val externalFilesDir: File
        get() = externalFilesDir()

    val externalFilesDirs: List<File>
        get() = externalFilesDirs()

    val externalCacheDirs: List<File>
        get() = ContextCompat.getExternalCacheDirs(appContext).filterNotNull()

    val externalCacheDir: File
        get() = externalCacheDirs.firstOrNull() ?: error("No external cache dir!")

    val obbDirs: List<File>
        get() = ContextCompat.getObbDirs(appContext).filterNotNull()

    val dataDir: File
        get() = ContextCompat.getDataDir(appContext) ?: error("No data dir!")

    val filesDir: File
        get() = appContext.filesDir

    val cacheDir: File
        get() = appContext.cacheDir

    val codeCacheDir: File
        get() = ContextCompat.getCodeCacheDir(appContext)

    val noBackupFilesDir: File?
        get() = ContextCompat.getNoBackupFilesDir(appContext)
}