package tool.xfy9326.schedule.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.getSystemService
import tool.xfy9326.schedule.kt.APP_ID
import java.io.File

object DownloadUtils {
    private const val DOWNLOAD_SUB_DIR = APP_ID

    fun requestDownloadFileDirectly(
        context: Context,
        url: String,
        fileName: String,
        title: String,
        description: String,
        mimeType: String,
    ): Long? {
        val subPath = DOWNLOAD_SUB_DIR + File.separator + fileName
        try {
            return context.getSystemService<DownloadManager>()?.enqueue(DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(title)
                setDescription(description)
                setMimeType(mimeType)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                try {
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, subPath)
                } catch (e: Exception) {
                    e.printStackTrace()
                    setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, subPath)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun requestDownloadFileByBrowser(context: Context, url: String) {
        IntentUtils.openUrlInBrowser(context, url)
    }
}