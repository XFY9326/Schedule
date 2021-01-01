package tool.xfy9326.schedule.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext", "MemberVisibilityCanBePrivate")
object ImageIO {
    private const val DEFAULT_BITMAP_SAVE_QUALITY = 100

    suspend fun readImage(file: File) = readImage(file.inputStream())

    suspend fun readImage(context: Context, uri: Uri) =
        context.contentResolver.openInputStream(uri)?.let {
            readImage(it)
        }

    suspend fun saveImage(
        file: File,
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat,
        quality: Int = DEFAULT_BITMAP_SAVE_QUALITY,
        recycle: Boolean = false,
    ) = if (BaseIO.prepareFileFolder(file)) saveImage(file.outputStream(), bitmap, compressFormat, quality, recycle) else false

    suspend fun saveImage(
        context: Context,
        uri: Uri,
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat,
        quality: Int = DEFAULT_BITMAP_SAVE_QUALITY,
        recycle: Boolean = false,
    ) = context.contentResolver.openOutputStream(uri)?.let {
        saveImage(it, bitmap, compressFormat, quality, recycle)
    } ?: false

    private suspend fun saveImage(outputStream: OutputStream, bitmap: Bitmap, compressFormat: Bitmap.CompressFormat, quality: Int, recycle: Boolean) =
        withContext(Dispatchers.IO) {
            try {
                if (!bitmap.isRecycled) {
                    outputStream.use {
                        val result = bitmap.compress(compressFormat, quality, it)
                        it.flush()

                        if (recycle) bitmap.recycle()

                        return@withContext result
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            false
        }

    private suspend fun readImage(inputStream: InputStream) = withContext(Dispatchers.IO) {
        try {
            return@withContext BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    suspend fun importImageFromUri(
        context: Context,
        fromUri: Uri,
        toFile: File,
        compressFormat: Bitmap.CompressFormat,
        quality: Int = DEFAULT_BITMAP_SAVE_QUALITY,
    ): Boolean {
        try {
            readImage(context, fromUri)?.let {
                return saveImage(toFile, it, compressFormat, quality, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}