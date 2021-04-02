@file:Suppress("BlockingMethodInNonBlockingContext", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.io

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kotlinx.serialization.json.Json
import okio.sink
import okio.source
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.io.kt.*
import tool.xfy9326.schedule.utils.IntentUtils
import java.io.File

object FileManager {
    private const val FILE_NAME_CRASH_RECORD = "LastCrashMills.record"

    private val FILE_EULA = rawResFile(R.raw.eula)
    private val FILE_LICENSE = rawResFile(R.raw.license)
    private val FILE_CRASH_RECORD = PathManager.LogDir.asParentOf(FILE_NAME_CRASH_RECORD)

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

    suspend fun readCrashLog(name: String) = runSafeIOJob {
        PathManager.LogDir.asParentOf(name).source().useBuffer {
            readUtf8()
        }
    }

    suspend fun writeCrashLog(name: String, content: String) = runOnlyResultIOJob {
        PathManager.LogDir.asParentOf(name).sink().useBuffer {
            writeUtf8(content)
        }
        true
    }

    suspend fun copyLogFile(name: String, uri: Uri) = copyFileToUri(PathManager.LogDir.asParentOf(name), uri)

    suspend fun readCrashRecord(): Long = runSafeIOJob(0L) {
        FILE_CRASH_RECORD.source().useBuffer {
            readUtf8().toLong()
        }
    }

    suspend fun writeCrashRecord(mills: Long) = runOnlyResultIOJob {
        FILE_CRASH_RECORD.sink().useBuffer {
            writeUtf8(mills.toString())
        }
        true
    }

    suspend fun copyBitmap(from: Uri, to: File, format: Bitmap.CompressFormat, quality: Int = 100) = runOnlyResultIOJob {
        val input = from.source()?.useBuffer {
            readBitmap()
        } ?: error("Can't read bitmap from $from")
        val writeResult = if (to.createParentFolder()) {
            to.sink().useBuffer {
                writeBitmap(input, format, quality)
            }
        } else {
            false
        }
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
        if (imageFile.createParentFolder()) {
            val result = imageFile.sink().useBuffer {
                writeBitmap(bitmap, format, quality)
            }
            if (recycle) bitmap.tryRecycle()
            if (result) {
                return@runSafeIOJob IOManager.getUriForFile(IntentUtils.FILE_PROVIDER_AUTH, imageFile)
            }
        }
        null
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

    suspend inline fun <reified T> readJSON(uri: Uri, json: Json) = runSafeIOJob {
        uri.source()?.useBuffer { readJSON<T>(json) }
    }

    suspend inline fun <reified T> writeJSON(uri: Uri, data: T, json: Json) = runOnlyResultIOJob {
        uri.sink()?.useBuffer {
            writeJSON(json, data)
            true
        } ?: false
    }
}