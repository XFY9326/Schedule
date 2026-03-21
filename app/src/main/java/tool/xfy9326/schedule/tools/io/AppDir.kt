@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.tools.io

import androidx.core.content.ContextCompat
import tool.xfy9326.schedule.tools.core.AppContext
import tool.xfy9326.schedule.tools.io.utils.joinToFilePath
import java.io.File

object AppDir {
    fun externalFilesDirs(vararg childDirs: String): List<File> =
        AppContext.getExternalFilesDirs(childDirs.joinToFilePath()).filterNotNull()

    fun externalFilesDir(vararg childDirs: String): File =
        externalFilesDirs(*childDirs).firstOrNull() ?: error("No external files dir available!")

    val externalFilesDir: File
        get() = externalFilesDir()

    val externalFilesDirs: List<File>
        get() = externalFilesDirs()

    val externalCacheDirs: List<File>
        get() = AppContext.externalCacheDirs.filterNotNull()

    val externalCacheDir: File
        get() = externalCacheDirs.firstOrNull() ?: error("No external cache dir available!")

    val obbDirs: List<File>
        get() = AppContext.obbDirs.filterNotNull()

    val dataDir: File
        get() = ContextCompat.getDataDir(AppContext) ?: error("No data dir available!")

    val filesDir: File
        get() = AppContext.filesDir

    val cacheDir: File
        get() = AppContext.cacheDir

    val codeCacheDir: File
        get() = ContextCompat.getCodeCacheDir(AppContext)

    val noBackupFilesDir: File?
        get() = ContextCompat.getNoBackupFilesDir(AppContext)
}