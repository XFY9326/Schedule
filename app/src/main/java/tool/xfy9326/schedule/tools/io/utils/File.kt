@file:Suppress("unused")

package tool.xfy9326.schedule.tools.io.utils

import android.net.Uri
import androidx.core.content.FileProvider
import tool.xfy9326.schedule.tools.core.AppContext
import java.io.File

fun filePath(start: String, vararg str: String): String =
    if (str.isEmpty()) {
        start
    } else if (str.size == 1) {
        start + File.separator + str.first()
    } else {
        start + File.separator + str.joinToString(File.separator)
    }

fun Array<out String>.joinToFilePath(): String? =
    if (isEmpty()) {
        null
    } else if (size == 1) {
        first()
    } else if (size == 2) {
        this[0] + File.separator + this[1]
    } else {
        joinToString(File.separator)
    }

fun File.asParentOf(vararg path: String): File =
    path.joinToFilePath()?.let { File(this, it) } ?: this

fun Iterable<File>.deleteRecursively(): Boolean =
    map { it.deleteRecursively() }.all { it }

fun Array<File>.deleteRecursively(): Boolean =
    map { it.deleteRecursively() }.all { it }

fun Sequence<File>.deleteRecursively(): Boolean =
    map { it.deleteRecursively() }.all { it }

fun File.takeIfExists(): File? =
    takeIf { it.exists() }

fun File.takeIfIsFile(): File? =
    takeIf { it.isFile }

fun File.takeIfIsDirectory(): File? =
    takeIf { it.isDirectory }

fun File.prepareParentFolder(): Boolean =
    parentFile.let { it == null || it.exists() || it.mkdirs() }

@Throws(FileSystemException::class)
fun File.preparedParentFolder(): File =
    if (prepareParentFolder()) {
        this
    } else {
        throw FileSystemException(file = this, reason = "Failed to create parent directory.")
    }

inline fun File.withPreparedParentFolder(crossinline block: (File) -> Unit) {
    if (prepareParentFolder()) block(this)
}

internal fun File.checkOverwrite(overwrite: Boolean) {
    if (exists()) {
        if (!overwrite)
            throw FileAlreadyExistsException(file = this, reason = "The destination file already exists.")
        else if (!delete())
            throw FileAlreadyExistsException(file = this, reason = "Tried to overwrite the destination, but failed to delete it.")
    }
}

@Throws(IllegalAccessException::class)
fun File.getUriByFileProvider(fileProviderAuth: String, displayName: String? = null): Uri =
    if (displayName == null) {
        FileProvider.getUriForFile(AppContext, fileProviderAuth, this)
    } else {
        FileProvider.getUriForFile(AppContext, fileProviderAuth, this, displayName)
    }