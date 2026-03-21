package tool.xfy9326.schedule.tools.crash

import android.content.Context

interface CrashLogGenerator {
    fun onGenerateCrashLog(context: Context, scene: String?, thread: Thread, throwable: Throwable): String
}