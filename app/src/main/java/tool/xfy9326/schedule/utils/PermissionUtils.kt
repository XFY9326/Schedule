package tool.xfy9326.schedule.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.kt.withTryLock


object PermissionUtils {
    private val permissionRequestLock = Mutex()

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
                val invalidPermissions = ArrayList<String>()
                for (permission in permissions) {
                    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
                    ) {
                        invalidPermissions.add(permission)
                    }
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