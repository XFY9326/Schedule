@file:Suppress("unused")

package tool.xfy9326.schedule.tools.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi

class PackageInstallPermissionContract : ActivityResultContract<Nothing?, Boolean>() {
    override fun createIntent(context: Context, input: Nothing?): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, context.packageUri)
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK
}

class AccessibilitySettingsContract : ActivityResultContract<Nothing?, Boolean>() {
    override fun createIntent(context: Context, input: Nothing?): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK
}

class ApplicationDetailSettingsContract : ActivityResultContract<Nothing?, Boolean>() {
    override fun createIntent(context: Context, input: Nothing?): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, context.packageUri)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK
}

@RequiresApi(Build.VERSION_CODES.S)
class ScheduleExactAlarmPermissionContract : ActivityResultContract<Nothing?, Boolean>() {
    override fun createIntent(context: Context, input: Nothing?): Intent =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, context.packageUri)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK
}
