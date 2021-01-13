package tool.xfy9326.schedule.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.tryStartActivity
import tool.xfy9326.schedule.tools.MIMEConst

object IntentUtils {
    const val FILE_PROVIDER_AUTH = BuildConfig.APPLICATION_ID + ".file.provider"

    fun openUrlInBrowser(context: Context, url: String) {
        context.tryStartActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun getSelectImageFromDocumentIntent() =
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = MIMEConst.MIME_IMAGE
            addCategory(Intent.CATEGORY_OPENABLE)
        }

    fun getSelectJsonFromDocumentIntent() =
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = MIMEConst.MIME_APPLICATION_JSON
            addCategory(Intent.CATEGORY_OPENABLE)
        }

    fun getShareImageIntent(context: Context, uri: Uri): Intent =
        Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = MIMEConst.MIME_IMAGE
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_MIME_TYPES, MIMEConst.MIME_IMAGE)
        }, context.getString(R.string.share_image))

    fun getCreateNewDocumentIntent(fileName: String, mimeType: String? = null) =
        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = mimeType ?: MIMEConst.MIME_ALL
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
}