package tool.xfy9326.schedule.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import lib.xfy9326.io.utils.asParentOf
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.tryStartActivity
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.utils.file.PathManager
import java.util.*

object IntentUtils {
    const val FILE_PROVIDER_AUTH = BuildConfig.APPLICATION_ID + ".file.provider"

    fun openUrlInBrowser(context: Context, url: String) {
        context.tryStartActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun getShareImageIntent(context: Context, uri: Uri): Intent =
        Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = MIMEConst.MIME_IMAGE
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_MIME_TYPES, MIMEConst.MIME_IMAGE)
        }, context.getString(R.string.share_image))

    fun sendCrashReport(context: Context, crashLogFileName: String) {
        val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTH, PathManager.LogDir.asParentOf(crashLogFileName))
        val mailAddress = context.getString(R.string.email)
        val mailTitle = context.getString(R.string.crash_report_email_title, context.getString(R.string.app_name))
        val mailContent = context.getString(
            R.string.crash_report_email_content,
            BuildConfig.APPLICATION_ID,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            Date().toString()
        )

        context.tryStartActivity(Intent(Intent.ACTION_SEND).apply {
            type = MIMEConst.MIME_TEXT
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_EMAIL, arrayOf(mailAddress))
            putExtra(Intent.EXTRA_SUBJECT, mailTitle)
            putExtra(Intent.EXTRA_TEXT, mailContent)
            putExtra(Intent.EXTRA_STREAM, uri)
        })
    }
}