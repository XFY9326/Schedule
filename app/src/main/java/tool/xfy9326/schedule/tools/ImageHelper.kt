package tool.xfy9326.schedule.tools

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import tool.xfy9326.schedule.io.ImageIO
import tool.xfy9326.schedule.kt.asParentOf
import java.io.File
import java.util.*

object ImageHelper {
    private const val IMAGE_PNG = "png"
    private const val IMAGE_JPEG = "jpeg"
    private const val IMAGE_WEBP = "webp"

    private val PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT = WEBPCompat

    suspend fun importImageFromUri(context: Context, fromUri: Uri, toDir: File, quality: Int): String? {
        val imageFile = generateNewImageFile(toDir, PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT)
        if (ImageIO.importImageFromUri(context, fromUri, imageFile, PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT, quality)) {
            return imageFile.name
        }
        return null
    }

    suspend fun outputImageToAlbum(bitmap: Bitmap, fileName: String) {

    }

    suspend fun shareImage(bitmap: Bitmap) {

    }

    @Suppress("SameParameterValue")
    private fun generateNewImageFile(dir: File, format: Bitmap.CompressFormat): File {
        require(dir.isDirectory) { "New image parent file must be directory!" }
        var newName: String
        val currentFilesNames = dir.list()
        do {
            newName = getNewFileNameByBitmapCompressFormat(UUID.randomUUID().toString(), format)
        } while (currentFilesNames != null && newName in currentFilesNames)
        return dir.asParentOf(newName)
    }

    private val WEBPCompat: Bitmap.CompressFormat
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }

    @Suppress("DEPRECATION")
    private fun isWEBP(format: Bitmap.CompressFormat?) =
        format == Bitmap.CompressFormat.WEBP || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                (format == Bitmap.CompressFormat.WEBP_LOSSY || format == Bitmap.CompressFormat.WEBP_LOSSLESS)

    private fun getNewFileNameByBitmapCompressFormat(fileName: String, compressFormat: Bitmap.CompressFormat): String {
        val pureFileName = fileName.substringBeforeLast('.')
        val newExtension = when {
            compressFormat == Bitmap.CompressFormat.JPEG -> IMAGE_JPEG
            compressFormat == Bitmap.CompressFormat.PNG -> IMAGE_PNG
            isWEBP(compressFormat) -> IMAGE_WEBP
            else -> error("Unknown bitmap compress format! $compressFormat")
        }
        return "$pureFileName.$newExtension"
    }
}