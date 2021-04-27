package tool.xfy9326.schedule.io.kt

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import okio.BufferedSink
import okio.BufferedSource

@Suppress("DEPRECATION")
fun Bitmap.CompressFormat.isWEBP() =
    this == Bitmap.CompressFormat.WEBP || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            (this == Bitmap.CompressFormat.WEBP_LOSSY || this == Bitmap.CompressFormat.WEBP_LOSSLESS)

fun Bitmap.tryRecycle() {
    if (!isRecycled) recycle()
}

fun BufferedSource.readBitmap(): Bitmap? = BitmapFactory.decodeStream(inputStream())

fun BufferedSink.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int = 100) =
    bitmap.compress(format, quality, outputStream()).also { flush() }