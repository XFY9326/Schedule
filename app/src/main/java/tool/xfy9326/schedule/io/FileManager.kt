@file:Suppress("BlockingMethodInNonBlockingContext", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.io

import android.graphics.Bitmap
import lib.xfy9326.android.kit.io.kt.rawResFile
import lib.xfy9326.android.kit.io.kt.source
import lib.xfy9326.android.kit.io.kt.useBuffer
import lib.xfy9326.android.kit.io.kt.writeBitmap
import lib.xfy9326.android.kit.tryRecycle
import lib.xfy9326.kit.asParentOf
import lib.xfy9326.kit.runSafeIOJob
import lib.xfy9326.kit.runUnsafeIOJob
import lib.xfy9326.kit.withPreparedDir
import okio.sink
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.io.utils.getUriByFileProvider

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
}