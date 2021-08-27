package tool.xfy9326.schedule.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import lib.xfy9326.android.kit.io.MIMEConst
import lib.xfy9326.android.kit.packageUri
import lib.xfy9326.kit.asArray
import lib.xfy9326.kit.asParentOf
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.io.PathManager
import tool.xfy9326.schedule.io.utils.getUriByFileProvider
import tool.xfy9326.schedule.kt.tryStartActivity
import java.util.*

object IntentUtils {
    const val COURSE_IMPORT_WIKI_URL = "https://github.com/XFY9326/Schedule/wiki"

    fun getLaunchAppIntent(context: Context) =
        context.packageManager.getLaunchIntentForPackage(context.packageName)!!.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    fun installPackage(context: Context, uri: Uri) {
        context.tryStartActivity(Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndType(uri, MIMEConst.MIME_APK)
        })
    }

    fun openUrlInBrowser(context: Context, url: String) {
        context.tryStartActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun seeImage(context: Context, uri: Uri) {
        context.tryStartActivity(Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndType(uri, MIMEConst.MIME_IMAGE)
        })
    }

    fun getShareImageIntent(context: Context, uri: Uri): Intent =
        Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = MIMEConst.MIME_IMAGE
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_MIME_TYPES, MIMEConst.MIME_IMAGE)
        }, context.getString(R.string.share_image))

    fun sendCrashReport(context: Context, logName: String) {
        val uri = PathManager.LogDir.asParentOf(logName).getUriByFileProvider()
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
            putExtra(Intent.EXTRA_EMAIL, mailAddress.asArray())
            putExtra(Intent.EXTRA_SUBJECT, mailTitle)
            putExtra(Intent.EXTRA_TEXT, mailContent)
            putExtra(Intent.EXTRA_STREAM, uri)
        })
    }

    fun openAPPDetailSettings(context: Context) {
        context.tryStartActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, context.packageUri))
    }
}