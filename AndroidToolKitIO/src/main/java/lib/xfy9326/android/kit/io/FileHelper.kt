@file:Suppress("BlockingMethodInNonBlockingContext")

package lib.xfy9326.android.kit.io

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import lib.xfy9326.android.kit.io.kt.*
import lib.xfy9326.android.kit.tryRecycle
import lib.xfy9326.kit.runOnlyResultIOJob
import lib.xfy9326.kit.runSafeIOJob
import lib.xfy9326.kit.withPreparedDir
import okio.sink
import okio.source
import java.io.File

object FileHelper {
    suspend fun copyBitmap(from: Uri, to: File, format: Bitmap.CompressFormat, quality: Int = 100) = runOnlyResultIOJob {
        val input = from.source()?.useBuffer {
            readBitmap()
        } ?: error("Can't read bitmap from $from")
        val writeResult = to.withPreparedDir {
            to.sink().useBuffer {
                writeBitmap(input, format, quality)
            }
        } ?: false
        input.recycle()
        writeResult
    }

    suspend fun writeBitmapToAlbum(bitmap: Bitmap, contentValues: ContentValues, format: Bitmap.CompressFormat, quality: Int = 100, recycle: Boolean = true) =
        runSafeIOJob {
            val uri = IOManager.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            val result = uri?.sink()?.useBuffer {
                writeBitmap(bitmap, format, quality)
            } ?: false
            if (recycle) bitmap.tryRecycle()
            if (result) {
                uri?.also { IOManager.requestScanMediaFile(it) }
            } else {
                null
            }
        }

    suspend fun copyFileToUri(from: File, to: Uri) = runOnlyResultIOJob {
        to.sink()?.useBuffer {
            writeAll(from.source())
            true
        } ?: false
    }
}