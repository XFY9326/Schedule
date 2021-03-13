@file:Suppress("BlockingMethodInNonBlockingContext")

package lib.xfy9326.io.processor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import lib.xfy9326.io.readProcessing
import lib.xfy9326.io.target.asTarget
import lib.xfy9326.io.target.base.InputTarget
import lib.xfy9326.io.target.base.OutputTarget
import lib.xfy9326.io.writeProcessing
import java.io.File
import java.io.InputStream
import java.io.OutputStream

fun File.bitmapReader() = asTarget().bitmapReader()

fun File.bitmapWriter(format: Bitmap.CompressFormat, quality: Int = 100, recycle: Boolean = true) = asTarget().bitmapWriter(format, quality, recycle)

fun Uri.bitmapReader() = asTarget().bitmapReader()

fun Uri.bitmapWriter(format: Bitmap.CompressFormat, quality: Int = 100, recycle: Boolean = true) = asTarget().bitmapWriter(format, quality, recycle)

fun InputTarget<out InputStream>.bitmapReader() = readProcessing(Bitmap::class) {
    BitmapFactory.decodeStream(this)
}

fun OutputTarget<out OutputStream>.bitmapWriter(format: Bitmap.CompressFormat, quality: Int = 100, recycle: Boolean = true) = writeProcessing(Bitmap::class) { data, _ ->
    data.compress(format, quality, this)
    flush()

    try {
        if (recycle && !data.isRecycled) data.recycle()
    } catch (e: Exception) {
    }
}