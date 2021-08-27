@file:Suppress("MemberVisibilityCanBePrivate")

package lib.xfy9326.android.kit.io

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import lib.xfy9326.android.kit.isWEBP
import lib.xfy9326.kit.asParentOf
import java.io.File
import java.util.*

object ImageHelper {
    const val IMAGE_PNG = "png"
    const val IMAGE_JPEG = "jpeg"
    const val IMAGE_WEBP = "webp"

    fun createImageContentValues(
        fileName: String,
        compressFormat: Bitmap.CompressFormat,
        imageWidth: Int,
        imageHeight: Int,
        mainDir: String,
        subDir: String? = null,
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
            } + if (subDir == null) {
                "$mainDir${File.separator}"
            } else {
                "$mainDir${File.separator}$subDir${File.separator}"
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, picturePath)
                put(MediaStore.Images.ImageColumns.DATE_TAKEN, createTime)
            } else {
                @Suppress("DEPRECATION")
                put(MediaStore.Images.Media.DATA, picturePath + fileName)
            }
        }

    fun generateNewImageFile(dir: File, format: Bitmap.CompressFormat): File {
        require(dir.isDirectory) { "New image parent file must be directory!" }
        var newName: String
        val currentFilesNames = dir.list()
        do {
            newName = getNewFileNameByBitmapCompressFormat(UUID.randomUUID().toString(), format)
        } while (currentFilesNames != null && newName in currentFilesNames)
        return dir.asParentOf(newName)
    }

    fun getNewFileNameByBitmapCompressFormat(fileName: String, compressFormat: Bitmap.CompressFormat): String {
        val pureFileName = fileName.substringBeforeLast('.')
        val newExtension = when {
            compressFormat == Bitmap.CompressFormat.JPEG -> IMAGE_JPEG
            compressFormat == Bitmap.CompressFormat.PNG -> IMAGE_PNG
            compressFormat.isWEBP() -> IMAGE_WEBP
            else -> error("Unknown bitmap compress format! $compressFormat")
        }
        return "$pureFileName.$newExtension"
    }

    fun getMimeTypeByBitmapCompressFormat(compressFormat: Bitmap.CompressFormat) =
        when {
            compressFormat == Bitmap.CompressFormat.JPEG -> MIMEConst.MIME_IMAGE_JPEG
            compressFormat == Bitmap.CompressFormat.PNG -> MIMEConst.MIME_IMAGE_PNG
            compressFormat.isWEBP() -> MIMEConst.MIME_IMAGE_WEBP
            else -> error("Unknown bitmap compress format! $compressFormat")
        }
}