package lib.xfy9326.io.utils

import androidx.core.content.ContextCompat
import lib.xfy9326.io.IOManager
import java.io.File

fun externalFilesDirs(vararg childDirs: String): List<File> {
    val dirType = if (childDirs.isEmpty()) {
        null
    } else {
        childDirs.joinToString(File.separator)
    }

    return ContextCompat.getExternalFilesDirs(IOManager.context, dirType).filterNotNull()
}

fun externalFilesDir(vararg childDirs: String) = externalFilesDirs(*childDirs).firstOrNull() ?: error("No external files dir!")

fun externalCacheDirs() = ContextCompat.getExternalCacheDirs(IOManager.context).filterNotNull()

fun externalCacheDir() = externalCacheDirs().firstOrNull() ?: error("No external cache dir!")

fun obbDirs() = ContextCompat.getObbDirs(IOManager.context).filterNotNull()


fun dataDir(): File = ContextCompat.getDataDir(IOManager.context) ?: error("No internal files dir!")

fun filesDir(): File = IOManager.context.filesDir

fun cacheDir(): File = IOManager.context.cacheDir

fun codeCacheDir(): File = ContextCompat.getCodeCacheDir(IOManager.context)

fun noBackupFilesDir() = ContextCompat.getNoBackupFilesDir(IOManager.context)