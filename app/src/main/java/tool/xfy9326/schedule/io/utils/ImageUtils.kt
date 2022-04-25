@file:Suppress("BlockingMethodInNonBlockingContext")

package tool.xfy9326.schedule.io.utils

import android.graphics.Bitmap
import android.net.Uri
import io.github.xfy9326.atools.io.helper.ImageHelper
import io.github.xfy9326.atools.io.okio.copyBitmapTo
import io.github.xfy9326.atools.io.utils.WEBPCompat
import io.github.xfy9326.atools.io.utils.asParentOf
import io.github.xfy9326.atools.io.utils.newFileName
import io.github.xfy9326.atools.io.utils.runIOJob
import tool.xfy9326.schedule.io.FileManager
import tool.xfy9326.schedule.io.PathManager
import java.io.File
import java.util.*

object ImageUtils {
    private val PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT = WEBPCompat
    private val OUTPUT_BITMAP_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG

    private fun generateNewImageFile(dir: File): File {
        require(dir.isDirectory) { "New image parent file must be directory!" }
        var newName: String
        val currentFilesNames = dir.list()
        do {
            newName = PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT.newFileName(UUID.randomUUID().toString())
        } while (currentFilesNames != null && newName in currentFilesNames)
        return dir.asParentOf(newName)
    }

    suspend fun importImageFromUri(fromUri: Uri, toDir: File, quality: Int): String? {
        val imageFile = generateNewImageFile(toDir)
        val copyResult = runIOJob {
            fromUri.copyBitmapTo(imageFile, compressFormat = PRIVATE_STORAGE_BITMAP_COMPRESS_FORMAT, quality = quality)
        }.isSuccess
        return if (copyResult) {
            imageFile.name
        } else {
            null
        }
    }

    suspend fun outputImageToAlbum(bitmap: Bitmap, recycle: Boolean = true): Uri? {
        val newFileName = OUTPUT_BITMAP_COMPRESS_FORMAT.newFileName(UUID.randomUUID().toString())
        return ImageHelper.exportBitmapToPublicAlbum(bitmap, newFileName, PathManager.DIR_SCHEDULE, OUTPUT_BITMAP_COMPRESS_FORMAT, recycle = recycle).getOrNull()
    }

    suspend fun createShareCacheImage(bitmap: Bitmap, recycle: Boolean = true): Uri? {
        val newFileName = OUTPUT_BITMAP_COMPRESS_FORMAT.newFileName(UUID.randomUUID().toString())
        return FileManager.createShareImage(newFileName, bitmap, OUTPUT_BITMAP_COMPRESS_FORMAT, recycle = recycle)
    }
}