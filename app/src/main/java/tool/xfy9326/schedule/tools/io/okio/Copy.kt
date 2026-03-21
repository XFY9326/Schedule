@file:Suppress("unused", "UnusedReceiverParameter")

package tool.xfy9326.schedule.tools.io.okio

import android.graphics.Bitmap
import android.net.Uri
import okio.Sink
import okio.Source
import okio.buffer
import okio.sink
import okio.source
import okio.use
import tool.xfy9326.schedule.tools.io.file.AssetFile
import tool.xfy9326.schedule.tools.io.file.RawResFile
import tool.xfy9326.schedule.tools.io.utils.checkOverwrite
import tool.xfy9326.schedule.tools.io.utils.use
import java.io.File
import java.io.IOException

@Throws(IOException::class)
fun Source.copyTo(sink: Sink, closeAll: Boolean = true): Long {
    return if (closeAll) {
        use { input ->
            sink.use { output ->
                input.copyTo(output, false)
            }
        }
    } else {
        buffer().readAll(sink)
    }
}

@Throws(IOException::class)
fun AssetFile.copyTo(uri: Uri, append: Boolean = false): Long {
    return source().copyTo(uri.sink(if (append) "wa" else "w"))
}

@Throws(IOException::class)
fun AssetFile.copyTo(file: File, overwrite: Boolean = false, append: Boolean = false): Long {
    file.checkOverwrite(overwrite)
    return file.source().copyTo(file.sink(append))
}

@Throws(IOException::class)
fun RawResFile.copyTo(uri: Uri, append: Boolean = false): Long {
    return source().copyTo(uri.sink(if (append) "wa" else "w"))
}

@Throws(IOException::class)
fun RawResFile.copyTo(file: File, overwrite: Boolean = false, append: Boolean = false): Long {
    file.checkOverwrite(overwrite)
    return file.source().copyTo(file.sink(append))
}

@Throws(IOException::class)
fun File.copyTo(uri: Uri, append: Boolean = false): Long {
    if (!this.exists())
        throw NoSuchFileException(file = this, reason = "The source file doesn't exist.")
    else if (this.isDirectory)
        throw FileSystemException(file = this, reason = "The source file can't be directory.")

    return source().copyTo(uri.sink(if (append) "wa" else "w"))
}

@Throws(IOException::class)
fun Uri.copyTo(file: File, overwrite: Boolean = false, append: Boolean = false): Long {
    file.checkOverwrite(overwrite)
    return file.source().copyTo(file.sink(append))
}

@Throws(IOException::class)
fun Uri.copyTo(uri: Uri, append: Boolean = false): Long {
    if (this == uri) return 0
    return source().copyTo(uri.sink(if (append) "wa" else "w"))
}

// Copy image with specific compressFormat and quality
// If compressFormat is null, quality is useless
@Throws(IOException::class)
fun Uri.copyBitmapTo(target: File, overwrite: Boolean = false, compressFormat: Bitmap.CompressFormat? = null, quality: Int = 100) {
    if (compressFormat == null) {
        copyTo(target, overwrite)
    } else {
        target.checkOverwrite(overwrite)
        readBitmap()?.use { bitmap ->
            target.sink().useBuffer { writeBitmap(bitmap, compressFormat, quality) }
        } ?: throw IOException("Bitmap decode error! Uri: $this")
    }
}