package tool.xfy9326.schedule.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext


object PermissionUtils {
    private const val PERMISSION_REQUEST_TIMEOUT = 500L

    private val permissionRequestLock = Mutex()

    suspend fun checkCalendarPermission(context: Context, launcher: ActivityResultLauncher<Array<String>>) =
        checkPermission(context, launcher, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))

    fun checkGrantResults(grantResults: Map<String, Boolean>): Boolean {
        for (result in grantResults) {
            if (!result.value) return false
        }
        return true
    }

    private suspend fun checkPermission(context: Context, launcher: ActivityResultLauncher<Array<String>>, permissions: Array<String>) =
        withContext(Dispatchers.Main.immediate) {
            if (permissionRequestLock.tryLock()) {
                try {
                    val invalidPermissions = ArrayList<String>()
                    for (permission in permissions) {
                        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
                        ) {
                            invalidPermissions.add(permission)
                        }
                    }
                    return@withContext if (invalidPermissions.isEmpty()) {
                        true
                    } else {
                        launcher.launch(invalidPermissions.toTypedArray())
                        false
                    }
                } finally {
                    delay(PERMISSION_REQUEST_TIMEOUT)
                    permissionRequestLock.unlock()
                }
            } else {
                return@withContext false
            }
        }
}