package tool.xfy9326.schedule.tools

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import tool.xfy9326.schedule.io.ImageIO
import tool.xfy9326.schedule.kt.asParentOf
import tool.xfy9326.schedule.utils.DirUtils
import tool.xfy9326.schedule.utils.IntentUtils
import java.io.File
import java.util.*

object ImageHelper {
    const val MIME_IMAGE = "image/*"
    private const val MIME_IMAGE_PNG = "image/png"
    private const val MIME_IMAGE_WEBP = "image/webp"
    private const val MIME_IMAGE_JPEG = "image/jpeg"

    private const val IMAGE_PNG = "png"
    private const val IMAGE_JPEG = "jpeg"
    private const val IMAGE_WEBP = "webp"

    private val PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT = WEBPCompat
    private val OUTPUT_BITMAP_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG

    suspend fun importImageFromUri(context: Context, fromUri: Uri, toDir: File, quality: Int): String? {
        val imageFile = generateNewImageFile(toDir, PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT)
        if (ImageIO.importImageFromUri(context, fromUri, imageFile, PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT, quality)) {
            return imageFile.name
        }
        return null
    }

    suspend fun outputImageToAlbum(context: Context, bitmap: Bitmap): Uri? {
        val newFileName = getNewFileNameByBitmapCompressFormat(UUID.randomUUID().toString(), OUTPUT_BITMAP_COMPRESS_FORMAT)
        val imageContentValues = createImageContentValues(newFileName, OUTPUT_BITMAP_COMPRESS_FORMAT, bitmap.width, bitmap.height)
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageContentValues)
        return if (uri != null && ImageIO.saveImage(context, uri, bitmap, OUTPUT_BITMAP_COMPRESS_FORMAT)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                @Suppress("DEPRECATION")
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            }
            uri
        } else {
            null
        }
    }

    suspend fun createShareCacheImage(context: Context, bitmap: Bitmap): Uri? {
        val newFileName = getNewFileNameByBitmapCompressFormat(UUID.randomUUID().toString(), OUTPUT_BITMAP_COMPRESS_FORMAT)
        val newImageFile = DirUtils.SharedFileDir.asParentOf(newFileName)
        return if (ImageIO.saveImage(newImageFile, bitmap, OUTPUT_BITMAP_COMPRESS_FORMAT)) {
            FileProvider.getUriForFile(context, IntentUtils.FILE_PROVIDER_AUTH, newImageFile)
        } else {
            null
        }
    }

    private fun createImageContentValues(
        fileName: String,
        compressFormat: Bitmap.CompressFormat,
        imageWidth: Int,
        imageHeight: Int,
        subDirName: String? = null,
    ): ContentValues =
        ContentValues().apply {
            val createTime = System.currentTimeMillis() / 1000

            put(MediaStore.Images.Media.TITLE, fileName)
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, getMimeTypeByBitmapCompressFormat(compressFormat))
            put(MediaStore.Images.Media.DATE_ADDED, createTime)
            put(MediaStore.Images.ImageColumns.DATE_MODIFIED, createTime)
            put(MediaStore.Images.Media.WIDTH, imageWidth)
            put(MediaStore.Images.Media.HEIGHT, imageHeight)

            val picturePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${Environment.DIRECTORY_PICTURES}${File.separator}"
            } else {
                @Suppress("DEPRECATION")
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath}${File.separator}"
            } + if (subDirName == null) {
                "${DirUtils.DIR_SCHEDULE}${File.separator}"
            } else {
                "${DirUtils.DIR_SCHEDULE}${File.separator}$subDirName${File.separator}"
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, picturePath)
                put(MediaStore.Images.ImageColumns.DATE_TAKEN, createTime)
            } else {
                @Suppress("DEPRECATION")
                put(MediaStore.Images.Media.DATA, picturePath + fileName)
            }
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

    private fun getMimeTypeByBitmapCompressFormat(compressFormat: Bitmap.CompressFormat) =
        when {
            compressFormat == Bitmap.CompressFormat.JPEG -> MIME_IMAGE_JPEG
            compressFormat == Bitmap.CompressFormat.PNG -> MIME_IMAGE_PNG
            isWEBP(compressFormat) -> MIME_IMAGE_WEBP
            else -> error("Unknown bitmap compress format! $compressFormat")
        }
}