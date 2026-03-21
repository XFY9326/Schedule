package tool.xfy9326.schedule.tools.io.helper

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.tools.io.IOManager
import tool.xfy9326.schedule.tools.io.okio.sink
import tool.xfy9326.schedule.tools.io.okio.useBuffer
import tool.xfy9326.schedule.tools.io.okio.writeBitmap
import tool.xfy9326.schedule.tools.io.utils.createPublicExportContentValues
import tool.xfy9326.schedule.tools.io.utils.requestScanMediaFile
import tool.xfy9326.schedule.tools.io.utils.tryRecycle

suspend fun Bitmap.exportToPublicAlbum(
    fileName: String,
    fileRelativeDir: String,
    compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100,
    recycle: Boolean = false,
    displayName: String = fileName,
    createMills: Long = System.currentTimeMillis()
): Result<Uri> = withContext(Dispatchers.IO) {
    runCatching {
        val contentValues = createPublicExportContentValues(fileName, fileRelativeDir, compressFormat, displayName, createMills)
        val uri = IOManager.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: error("External content media uri create failed!")
        uri.sink().useBuffer { writeBitmap(this@exportToPublicAlbum, compressFormat, quality) }
        if (recycle) tryRecycle()
        uri.also { it.requestScanMediaFile() }
    }
}