package tool.xfy9326.schedule.io

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.sink
import tool.xfy9326.schedule.io.utils.getUriByFileProvider
import tool.xfy9326.schedule.tools.io.okio.useBuffer
import tool.xfy9326.schedule.tools.io.okio.writeBitmap
import tool.xfy9326.schedule.tools.io.utils.asParentOf
import tool.xfy9326.schedule.tools.io.utils.preparedParentFolder
import tool.xfy9326.schedule.tools.io.utils.tryRecycle

object FileManager {
    fun getAppPictureFile(name: String) = PathManager.PictureAppDir.asParentOf(name)

    suspend fun createShareImage(name: String, bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int = 100, recycle: Boolean = true): Uri? =
        withContext(Dispatchers.IO) {
            runCatching {
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
            }
        }.getOrNull()
}