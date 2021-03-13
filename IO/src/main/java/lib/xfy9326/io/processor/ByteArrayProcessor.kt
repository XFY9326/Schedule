@file:Suppress("BlockingMethodInNonBlockingContext")

package lib.xfy9326.io.processor

import android.graphics.Bitmap
import android.net.Uri
import lib.xfy9326.io.readProcessing
import lib.xfy9326.io.target.asTarget
import lib.xfy9326.io.target.base.InputTarget
import lib.xfy9326.io.target.base.OutputTarget
import lib.xfy9326.io.writeProcessing
import java.io.File
import java.io.InputStream
import java.io.OutputStream

fun File.byteArrayReader() = asTarget().bitmapReader()

fun File.byteArrayWriter(format: Bitmap.CompressFormat, quality: Int = 100, recycle: Boolean = true) = asTarget().bitmapWriter(format, quality, recycle)

fun Uri.byteArrayReader() = asTarget().bitmapReader()

fun Uri.byteArrayWriter(format: Bitmap.CompressFormat, quality: Int = 100, recycle: Boolean = true) = asTarget().bitmapWriter(format, quality, recycle)

fun InputTarget<out InputStream>.byteArrayReader() = readProcessing(ByteArray::class) {
    readBytes()
}

fun OutputTarget<out OutputStream>.byteArrayWriter() = writeProcessing(ByteArray::class) { data, _ ->
    write(data)
    flush()
}