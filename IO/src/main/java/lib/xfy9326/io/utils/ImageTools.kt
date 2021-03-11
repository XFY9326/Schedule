package lib.xfy9326.io.utils

import android.graphics.Bitmap
import android.os.Build

@Suppress("DEPRECATION")
fun Bitmap.CompressFormat.isWEBP() =
    this == Bitmap.CompressFormat.WEBP || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            (this == Bitmap.CompressFormat.WEBP_LOSSY || this == Bitmap.CompressFormat.WEBP_LOSSLESS)