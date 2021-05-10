package tool.xfy9326.schedule.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.kt.withTryLock


object PermissionUtils {
    private val permissionRequestLock = Mutex()

    fun canInstallPackage(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1
        }

    suspend fun checkCalendarPermission(context: Context, launcher: ActivityResultLauncher<Array<String>>) =
        checkPermission(context, launcher, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))

    fun checkGrantResults(grantResults: Map<String, Boolean>): Boolean {
        for (result in grantResults) {
            if (!result.value) return false
        }
        return true
    }

    private suspend fun checkPermission(context: Context, launcher: ActivityResultLauncher<Array<String>>, permissions: Array<String>): Boolean =
        withContext(Dispatchers.Main.immediate) {
            permissionRequestLock.withTryLock {
                val invalidPermissions = permissions.filter {
                    ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                }
                return@withTryLock if (invalidPermissions.isEmpty()) {
                    true
                } else {
                    launcher.launch(invalidPermissions.toTypedArray())
                    false
                }
            } ?: false
        }
}