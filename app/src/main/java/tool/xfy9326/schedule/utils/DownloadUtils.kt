package tool.xfy9326.schedule.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import io.github.xfy9326.atools.core.openUrlInBrowser
import java.io.File

object DownloadUtils {
    private const val DOWNLOAD_SUB_DIR = PROJECT_ID

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
            return context.applicationContext.getSystemService<DownloadManager>()?.enqueue(DownloadManager.Request(Uri.parse(url)).apply {
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
        context.openUrlInBrowser(url.toUri())
    }
}