package tool.xfy9326.schedule.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

object PermissionUtils {
    private const val PERMISSION_REQUEST_TIMEOUT = 500L

    private val permissionRequestLock = Mutex()

    suspend fun checkCalendarPermission(activity: Activity, requestCode: Int) =
        checkPermission(
            activity = activity,
            requestCode = requestCode,
            permissions = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        )

    suspend fun checkCalendarPermission(fragment: Fragment, requestCode: Int) =
        checkPermission(
            fragment = fragment,
            requestCode = requestCode,
            permissions = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        )

    fun checkGrantResults(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    private suspend fun checkPermission(activity: Activity? = null, fragment: Fragment? = null, requestCode: Int, permissions: Array<String>) =
        withContext(Dispatchers.Main.immediate) {
            require(activity != null || fragment != null)

            if (permissionRequestLock.tryLock()) {
                try {
                    val invalidPermissions = ArrayList<String>()
                    for (permission in permissions) {
                        if (ContextCompat.checkSelfPermission(activity ?: fragment!!.requireContext(),
                                permission) != PackageManager.PERMISSION_GRANTED
                        ) {
                            invalidPermissions.add(permission)
                        }
                    }
                    return@withContext if (invalidPermissions.isEmpty()) {
                        true
                    } else {
                        if (activity != null) {
                            ActivityCompat.requestPermissions(activity, invalidPermissions.toTypedArray(), requestCode)
                        } else {
                            fragment?.requestPermissions(invalidPermissions.toTypedArray(), requestCode)
                        }
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