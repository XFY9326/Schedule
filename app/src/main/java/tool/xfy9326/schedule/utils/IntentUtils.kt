package tool.xfy9326.schedule.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.tryStartActivity
import tool.xfy9326.schedule.tools.ImageHelper

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
            type = ImageHelper.MIME_IMAGE
            addCategory(Intent.CATEGORY_OPENABLE)
        }

    fun getShareImageIntent(context: Context, uri: Uri): Intent =
        Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = ImageHelper.MIME_IMAGE
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_MIME_TYPES, ImageHelper.MIME_IMAGE)
        }, context.getString(R.string.share_image))
}