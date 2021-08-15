package tool.xfy9326.schedule.io.utils

import android.graphics.Bitmap
import android.net.Uri
import lib.xfy9326.android.kit.io.FileHelper
import lib.xfy9326.android.kit.io.ImageHelper
import lib.xfy9326.android.kit.io.kt.WEBPCompat
import tool.xfy9326.schedule.io.FileManager
import tool.xfy9326.schedule.io.PathManager
import java.io.File
import java.util.*

object ImageUtils {
    private val PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT = WEBPCompat
    private val OUTPUT_BITMAP_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG

    suspend fun importImageFromUri(fromUri: Uri, toDir: File, quality: Int): String? {
        val imageFile = ImageHelper.generateNewImageFile(toDir, PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT)

        return if (FileHelper.copyBitmap(fromUri, imageFile, PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT, quality)) {
            imageFile.name
        } else {
            null
        }
    }

    suspend fun outputImageToAlbum(bitmap: Bitmap, recycle: Boolean = true): Uri? {
        val newFileName = ImageHelper.getNewFileNameByBitmapCompressFormat(UUID.randomUUID().toString(), OUTPUT_BITMAP_COMPRESS_FORMAT)
        val imageContentValues = ImageHelper.createImageContentValues(newFileName, OUTPUT_BITMAP_COMPRESS_FORMAT, bitmap.width, bitmap.height, PathManager.DIR_SCHEDULE)
        return FileHelper.writeBitmapToAlbum(bitmap, imageContentValues, OUTPUT_BITMAP_COMPRESS_FORMAT, recycle = recycle)
    }

    suspend fun createShareCacheImage(bitmap: Bitmap, recycle: Boolean = true): Uri? {
        val newFileName = ImageHelper.getNewFileNameByBitmapCompressFormat(UUID.randomUUID().toString(), OUTPUT_BITMAP_COMPRESS_FORMAT)
        return FileManager.createShareImage(newFileName, bitmap, OUTPUT_BITMAP_COMPRESS_FORMAT, recycle = recycle)
    }
}