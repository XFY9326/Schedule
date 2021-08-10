@file:Suppress("BlockingMethodInNonBlockingContext", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.io

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import lib.xfy9326.android.kit.tryRecycle
import lib.xfy9326.kit.*
import okio.sink
import okio.source
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.io.IOManager.getUriByFileProvider
import tool.xfy9326.schedule.io.kt.*
import java.io.File

object FileManager {
    private val FILE_EULA = rawResFile(R.raw.eula)
    private val FILE_LICENSE = rawResFile(R.raw.license)

    suspend fun readEULA() = runUnsafeIOJob {
        FILE_EULA.source().useBuffer {
            readUtf8()
        }
    }

    suspend fun readLicense() = runUnsafeIOJob {
        FILE_LICENSE.source().useBuffer {
            readUtf8()
        }
    }

    fun getAppPictureFile(name: String) = PathManager.PictureAppDir.asParentOf(name)

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

    suspend fun createShareImage(name: String, bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int = 100, recycle: Boolean = true) = runSafeIOJob {
        val imageFile = PathManager.SharedFileDir.asParentOf(name)
        imageFile.withPreparedDir {
            val result = imageFile.sink().useBuffer {
                writeBitmap(bitmap, format, quality)
            }
            if (recycle) bitmap.tryRecycle()
            if (result) {
                imageFile.getUriByFileProvider()
            } else {
                null
            }
        }
    }

    suspend fun writeText(uri: Uri, text: String?) = runOnlyResultIOJob {
        uri.sink()?.useBuffer {
            writeUtf8(text.orEmpty())
            true
        } ?: false
    }

    suspend fun copyFileToUri(from: File, to: Uri) = runOnlyResultIOJob {
        to.sink()?.useBuffer {
            writeAll(from.source())
            true
        } ?: false
    }
}