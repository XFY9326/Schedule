@file:Suppress("BlockingMethodInNonBlockingContext", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.io

import android.graphics.Bitmap
import android.net.Uri
import io.github.xfy9326.atools.io.file.rawResFile
import io.github.xfy9326.atools.io.okio.source
import io.github.xfy9326.atools.io.okio.useBuffer
import io.github.xfy9326.atools.io.okio.writeBitmap
import io.github.xfy9326.atools.io.utils.asParentOf
import io.github.xfy9326.atools.io.utils.preparedParentFolder
import io.github.xfy9326.atools.io.utils.runIOJob
import io.github.xfy9326.atools.io.utils.tryRecycle
import okio.sink
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.io.utils.getUriByFileProvider

object FileManager {
    private val FILE_EULA = rawResFile(R.raw.eula)
    private val FILE_LICENSE = rawResFile(R.raw.license)

    suspend fun readEULA() = runIOJob {
        FILE_EULA.source().useBuffer {
            readUtf8()
        }
    }.getOrThrow()

    suspend fun readLicense() = runIOJob {
        FILE_LICENSE.source().useBuffer {
            readUtf8()
        }
    }.getOrThrow()

    fun getAppPictureFile(name: String) = PathManager.PictureAppDir.asParentOf(name)

    suspend fun createShareImage(name: String, bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int = 100, recycle: Boolean = true): Uri? = runIOJob {
        val imageFile = PathManager.SharedFileDir.asParentOf(name)
        imageFile.preparedParentFolder().let {
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
    }.getOrNull()
}