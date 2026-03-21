@file:Suppress("unused")

package tool.xfy9326.schedule.tools.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.core.content.IntentCompat
import tool.xfy9326.schedule.tools.base.castNullable
import java.io.Serializable

private const val MIME_APK = "application/vnd.android.package-archive"

fun Context.getLaunchAppIntent(): Intent? =
    packageManager.getLaunchIntentForPackage(packageName)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

fun Context.installPackage(uri: Uri): Boolean =
    tryStartActivity(Intent(Intent.ACTION_VIEW).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        setDataAndType(uri, MIME_APK)
    })

fun Context.openUrlInBrowser(uri: Uri): Boolean =
    tryStartActivity(Intent(Intent.ACTION_VIEW).apply {
        data = uri
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })

inline fun <reified T : Serializable> Intent.getSerializableExtraCompat(name: String?): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializableExtra(name).castNullable()
    }
}

inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String?): T? {
    return IntentCompat.getParcelableExtra(this, name, T::class.java)
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayExtraCompat(name: String?): Array<out T>? {
    return IntentCompat.getParcelableArrayExtra(this, name, T::class.java).castNullable()
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(name: String?): ArrayList<T>? {
    return IntentCompat.getParcelableArrayListExtra(this, name, T::class.java)
}
