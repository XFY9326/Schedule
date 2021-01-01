package tool.xfy9326.schedule.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import tool.xfy9326.schedule.kt.tryStartActivity

object IntentUtils {
    private const val MIME_IMAGE = "image/*"

    fun openUrlInBrowser(context: Context, url: String) {
        context.tryStartActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun getSelectImageFromDocumentIntent() =
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = MIME_IMAGE
            addCategory(Intent.CATEGORY_OPENABLE)
        }
}