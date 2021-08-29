package lib.xfy9326.android.kit

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService

object PermissionCompat {
    fun canInstallPackage(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1
        }

    fun canScheduleNextAlarm(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService<AlarmManager>()?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
}