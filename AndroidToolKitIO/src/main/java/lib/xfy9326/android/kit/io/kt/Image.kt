package lib.xfy9326.android.kit.io.kt

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import okio.BufferedSink
import okio.BufferedSource

val WEBPCompat: Bitmap.CompressFormat
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSY
    } else {
        @Suppress("DEPRECATION")
        Bitmap.CompressFormat.WEBP
    }

fun BufferedSource.readBitmap(): Bitmap? = BitmapFactory.decodeStream(inputStream())

fun BufferedSink.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int = 100) =
    bitmap.compress(format, quality, outputStream()).also { flush() }