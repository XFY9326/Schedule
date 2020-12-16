package tool.xfy9326.schedule.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import tool.xfy9326.schedule.kt.tryStartActivity

object IntentUtils {
    fun openUrlInBrowser(context: Context, url: String) {
        context.tryStartActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}