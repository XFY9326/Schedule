package tool.xfy9326.schedule.io.utils

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import lib.xfy9326.android.kit.isWEBP
import lib.xfy9326.kit.asParentOf
import tool.xfy9326.schedule.io.FileManager
import tool.xfy9326.schedule.io.PathManager
import tool.xfy9326.schedule.tools.MIMEConst
import java.io.File
import java.util.*

object ImageUtils {
    private const val IMAGE_PNG = "png"
    private const val IMAGE_JPEG = "jpeg"
    private const val IMAGE_WEBP = "webp"

    private val PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT = WEBPCompat
    private val OUTPUT_BITMAP_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG

    suspend fun importImageFromUri(fromUri: Uri, toDir: File, quality: Int): String? {
        val imageFile = generateNewImageFile(toDir, PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT)

        return if (FileManager.copyBitmap(fromUri, imageFile, PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT, quality)) {
            imageFile.name
        } else {
            null
        }
    }

    suspend fun outputImageToAlbum(bitmap: Bitmap, recycle: Boolean = true): Uri? {
        val newFileName = getNewFileNameByBitmapCompressFormat(UUID.randomUUID().toString(), OUTPUT_BITMAP_COMPRESS_FORMAT)
        val imageContentValues = createImageContentValues(newFileName, OUTPUT_BITMAP_COMPRESS_FORMAT, bitmap.width, bitmap.height)
        return FileManager.writeBitmapToAlbum(bitmap, imageContentValues, OUTPUT_BITMAP_COMPRESS_FORMAT, recycle = recycle)
    }

    suspend fun createShareCacheImage(bitmap: Bitmap, recycle: Boolean = true): Uri? {
        val newFileName = getNewFileNameByBitmapCompressFormat(UUID.randomUUID().toString(), OUTPUT_BITMAP_COMPRESS_FORMAT)
        return FileManager.createShareImage(newFileName, bitmap, OUTPUT_BITMAP_COMPRESS_FORMAT, recycle = recycle)
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
                "${PathManager.DIR_SCHEDULE}${File.separator}"
            } else {
                "${PathManager.DIR_SCHEDULE}${File.separator}$subDirName${File.separator}"
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

    private fun getNewFileNameByBitmapCompressFormat(fileName: String, compressFormat: Bitmap.CompressFormat): String {
        val pureFileName = fileName.substringBeforeLast('.')
        val newExtension = when {
            compressFormat == Bitmap.CompressFormat.JPEG -> IMAGE_JPEG
            compressFormat == Bitmap.CompressFormat.PNG -> IMAGE_PNG
            compressFormat.isWEBP() -> IMAGE_WEBP
            else -> error("Unknown bitmap compress format! $compressFormat")
        }
        return "$pureFileName.$newExtension"
    }

    private fun getMimeTypeByBitmapCompressFormat(compressFormat: Bitmap.CompressFormat) =
        when {
            compressFormat == Bitmap.CompressFormat.JPEG -> MIMEConst.MIME_IMAGE_JPEG
            compressFormat == Bitmap.CompressFormat.PNG -> MIMEConst.MIME_IMAGE_PNG
            compressFormat.isWEBP() -> MIMEConst.MIME_IMAGE_WEBP
            else -> error("Unknown bitmap compress format! $compressFormat")
        }
}