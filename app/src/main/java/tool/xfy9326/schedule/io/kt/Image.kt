package tool.xfy9326.schedule.io.kt

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okio.BufferedSink
import okio.BufferedSource

fun BufferedSource.readBitmap(): Bitmap? = BitmapFactory.decodeStream(inputStream())

fun BufferedSink.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int = 100) =
    bitmap.compress(format, quality, outputStream()).also { flush() }