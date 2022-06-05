package tool.xfy9326.schedule.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import io.github.xfy9326.atools.ui.PermissionCompat
import io.github.xfy9326.atools.ui.installPackage
import io.github.xfy9326.atools.ui.showGlobalToast
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppDataStore

class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            runBlocking {
                val downloadId = AppDataStore.apkUpdateDownloadIdFlow.firstOrNull()
                if (downloadId == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                    context.applicationContext.getSystemService<DownloadManager>()?.getUriForDownloadedFile(downloadId)?.let {
                        showGlobalToast(R.string.update_download_success)
                        if (PermissionCompat.canInstallPackage(context)) {
                            context.installPackage(it)
                        }
                        AppDataStore.removeApkUpdateDownloadId()
                    }
                }
            }
        }
    }
}