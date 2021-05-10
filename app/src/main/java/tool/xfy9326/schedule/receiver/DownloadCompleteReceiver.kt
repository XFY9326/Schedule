package tool.xfy9326.schedule.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.firstOrNull
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.kt.goAsync
import tool.xfy9326.schedule.kt.showGlobalToast
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.PermissionUtils

class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            goAsync {
                val downloadId = AppDataStore.apkUpdateDownloadIdFlow.firstOrNull()
                if (downloadId == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                    context.applicationContext.getSystemService<DownloadManager>()?.getUriForDownloadedFile(downloadId)?.let {
                        showGlobalToast(R.string.update_download_success)
                        if (PermissionUtils.canInstallPackage(context)) {
                            IntentUtils.installPackage(context, it)
                        }
                    }
                }
            }
        }
    }
}