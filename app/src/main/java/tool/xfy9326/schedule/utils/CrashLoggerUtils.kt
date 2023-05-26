package tool.xfy9326.schedule.utils

import android.content.Context
import io.github.xfy9326.atools.core.AppContext
import io.github.xfy9326.atools.crash.CrashLogger
import io.github.xfy9326.atools.crash.CrashLoggerStrategy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.io.PathManager
import tool.xfy9326.schedule.kt.appErrorRelaunch
import tool.xfy9326.schedule.kt.crashRelaunch

object CrashLoggerUtils {
    private const val CRASH_RELAUNCH_PERIOD_MILLS = 5000L

    fun init(context: Context) {
        val (enabled, maxLogAmount) = runBlocking {
            AppSettingsDataStore.handleExceptionFlow.first() to AppSettingsDataStore.debugLogsMaxStoreAmountFlow.first()
        }
        val crashLogger = CrashLogger.initInstance(
            context, true, CrashLoggerStrategy(
                crashLogDir = PathManager.LogDir,
                useDefaultExceptionHandler = false,
                maxLogAmount = maxLogAmount
            )
        )
        crashLogger.setEnabled(enabled)
        crashLogger.setOnCrashLoggedListener { lastCrashInfo, file ->
            val isAppErrorRelaunch = System.currentTimeMillis() - lastCrashInfo.lastCrashMills <= CRASH_RELAUNCH_PERIOD_MILLS
            if (isAppErrorRelaunch) {
                AppContext.appErrorRelaunch(file.absolutePath)
            } else {
                AppContext.crashRelaunch()
            }
        }
    }
}