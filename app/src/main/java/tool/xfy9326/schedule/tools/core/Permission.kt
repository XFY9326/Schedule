@file:Suppress("unused")

package tool.xfy9326.schedule.tools.core

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.checkSelfPermissions(permissions: Array<String>): Map<String, Boolean> =
    permissions.associateWith { isPermissionGranted(it) }

fun Context.canInstallPackage() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        packageManager.canRequestPackageInstalls()
    } else {
        @Suppress("DEPRECATION")
        Settings.Secure.getInt(contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1
    }

fun Context.canScheduleNextAlarm() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService<AlarmManager>()?.canScheduleExactAlarms() ?: false
    } else {
        true
    }

