package tool.xfy9326.schedule.tools.crash

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

private const val UNKNOWN_VERSION_NAME = "Unknown"
private const val UNKNOWN_VERSION_CODE = 0L

internal fun Context.appVersion(): Pair<String, Long> {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }.let {
            return@runCatching if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                it.versionName.orEmpty() to it.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                it.versionName.orEmpty() to it.versionCode.toLong()
            }
        }
    }.getOrDefault(UNKNOWN_VERSION_NAME to UNKNOWN_VERSION_CODE)
}