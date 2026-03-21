package tool.xfy9326.schedule.tools.io.utils

import android.graphics.Bitmap
import android.os.Build

val WEBPCompat: Bitmap.CompressFormat
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSY
    } else {
        @Suppress("DEPRECATION")
        Bitmap.CompressFormat.WEBP
    }

@Suppress("DEPRECATION")
fun Bitmap.CompressFormat.isWEBP() =
    this == Bitmap.CompressFormat.WEBP || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            (this == Bitmap.CompressFormat.WEBP_LOSSY || this == Bitmap.CompressFormat.WEBP_LOSSLESS)

fun Bitmap.tryRecycle() {
    runCatching {
        if (!isRecycled) recycle()
    }
}

inline fun <R> Bitmap.use(block: (Bitmap) -> R): R {
    try {
        return block(this)
    } finally {
        tryRecycle()
    }
}

object ImageMimeType {
    const val IMAGE = "image/*"
    const val PNG = "image/png"
    const val WEBP = "image/webp"
    const val JPEG = "image/jpeg"
}

fun Bitmap.CompressFormat.getMimeType(): String =
    when {
        this == Bitmap.CompressFormat.JPEG -> ImageMimeType.JPEG
        this == Bitmap.CompressFormat.PNG -> ImageMimeType.PNG
        this.isWEBP() -> ImageMimeType.WEBP
        else -> ImageMimeType.IMAGE
    }

object ImageFileExtension {
    const val PNG = "png"
    const val WEBP = "webp"
    const val JPEG = "jpeg"
}

fun Bitmap.CompressFormat.newFileName(fileName: String): String =
    when {
        this == Bitmap.CompressFormat.JPEG -> "$fileName.${ImageFileExtension.JPEG}"
        this == Bitmap.CompressFormat.PNG -> "$fileName.${ImageFileExtension.PNG}"
        this.isWEBP() -> "$fileName.${ImageFileExtension.WEBP}"
        else -> error("Unknown Bitmap.CompressFormat: $name")
    }