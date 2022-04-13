package lib.xfy9326.android.kit

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi

fun ClipData.getItemUriList(): List<Uri> {
    if (itemCount == 0) return emptyList()
    val result = ArrayList<Uri>(itemCount)
    for (i in 0 until itemCount) {
        getItemAt(i)?.uri?.let {
            result.add(it)
        }
    }
    return result
}

@RequiresApi(Build.VERSION_CODES.S)
class ScheduleNextAlarmPermissionContact : ActivityResultContract<Unit?, Boolean>() {
    override fun createIntent(context: Context, input: Unit?) =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, context.packageUri)

    override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == Activity.RESULT_OK
}

class PackageInstallPermissionContact : ActivityResultContract<Nothing, Boolean>() {
    override fun createIntent(context: Context, input: Nothing): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, context.packageUri)
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?) = PermissionCompat.canInstallPackage(ApplicationInstance)
}