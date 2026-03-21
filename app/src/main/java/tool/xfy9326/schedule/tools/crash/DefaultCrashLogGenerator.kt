package tool.xfy9326.schedule.tools.crash

import android.content.Context
import android.os.Build
import java.util.Date

internal class DefaultCrashLogGenerator : CrashLogGenerator {

    override fun onGenerateCrashLog(context: Context, scene: String?, thread: Thread, throwable: Throwable): String {
        val appVersion = context.appVersion()
        return buildString {
            appendLine(Date().toString())
            appendLine()
            appendLine("App Version: ${appVersion.first} (${appVersion.second})")
            appendLine("Device Name: ${Build.BRAND}  ${Build.MODEL}")
            appendLine("Device ABI: ${Build.SUPPORTED_ABIS?.joinToString()}")
            appendLine("Android Version: ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})")
            if (scene != null) appendLine("Scene: $scene")
            appendLine()
            appendLine(thread)
            appendLine(throwable.stackTraceToString())
        }
    }
}