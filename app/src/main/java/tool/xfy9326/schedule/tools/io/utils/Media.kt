package tool.xfy9326.schedule.tools.io.utils

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import tool.xfy9326.schedule.tools.core.AppContext

fun Uri.requestScanMediaFile() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        @Suppress("DEPRECATION")
        AppContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, this))
    }
}

// Create ContentValues which export this bitmap to public 'Picture' folder
fun Bitmap.createPublicExportContentValues(
    fileName: String,
    fileRelativeDir: String,
    compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    displayName: String = fileName,
    createMills: Long = System.currentTimeMillis()
): ContentValues =
    ContentValues().apply {
        put(MediaStore.Images.ImageColumns.TITLE, fileName)
        put(MediaStore.Images.ImageColumns.DISPLAY_NAME, displayName)
        put(MediaStore.Images.ImageColumns.MIME_TYPE, compressFormat.getMimeType())
        put(MediaStore.Images.ImageColumns.DATE_ADDED, createMills / 1000)
        put(MediaStore.Images.ImageColumns.DATE_MODIFIED, createMills / 1000)
        put(MediaStore.Images.ImageColumns.DATE_TAKEN, createMills)
        put(MediaStore.Images.ImageColumns.WIDTH, width)
        put(MediaStore.Images.ImageColumns.HEIGHT, height)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(
                MediaStore.Images.ImageColumns.RELATIVE_PATH,
                filePath(Environment.DIRECTORY_PICTURES, fileRelativeDir)
            )
        } else {
            put(
                MediaStore.Images.ImageColumns.DATA,
                filePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath, fileRelativeDir, fileName)
            )
        }
    }