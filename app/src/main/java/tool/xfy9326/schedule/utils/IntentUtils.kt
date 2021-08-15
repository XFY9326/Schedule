package tool.xfy9326.schedule.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import lib.xfy9326.android.kit.ApplicationInstance
import lib.xfy9326.android.kit.io.MIMEConst
import lib.xfy9326.android.kit.packageUri
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
            putExtra(Intent.EXTRA_EMAIL, arrayOf(mailAddress))
            putExtra(Intent.EXTRA_SUBJECT, mailTitle)
            putExtra(Intent.EXTRA_TEXT, mailContent)
            putExtra(Intent.EXTRA_STREAM, uri)
        })
    }

    fun openAPPDetailSettings(context: Context) {
        context.tryStartActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, context.packageUri))
    }

    @RequiresApi(Build.VERSION_CODES.S)
    class ScheduleNextAlarmPermissionContact : ActivityResultContract<Nothing, Boolean>() {
        override fun createIntent(context: Context, input: Nothing?) =
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, context.packageUri)

        override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == Activity.RESULT_OK
    }

    class PackageInstallPermissionContact : ActivityResultContract<Nothing, Boolean>() {
        override fun createIntent(context: Context, input: Nothing?): Intent {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, context.packageUri)
            } else {
                Intent(Settings.ACTION_SECURITY_SETTINGS)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?) = PermissionUtils.canInstallPackage(ApplicationInstance)
    }
}