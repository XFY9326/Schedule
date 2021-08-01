package tool.xfy9326.schedule.tools

import android.os.Build
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import lib.xfy9326.android.kit.ApplicationInstance
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.io.CrashFileManager
import tool.xfy9326.schedule.kt.appErrorRelaunch
import tool.xfy9326.schedule.kt.crashRelaunch
import java.util.*

object ExceptionHandler : Thread.UncaughtExceptionHandler {
    const val CRASH_LOG_FILE_EXTENSION = "log"

    private const val CRASH_RELAUNCH_PERIOD_MILLS = 5000L
    private const val CRASH_LOG_FILE_PREFIX = "Crash"
    private const val CRASH_LOG_FILE_CONNECT_SYMBOL = "_"

    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun init() {
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            var crashSaveSuccess = false
            val crashFileName = getNewCrashFileName()
            try {
                runBlocking {
                    CrashFileManager.writeCrashLog(crashFileName, generateCrashLog(t, e))
                    CrashFileManager.runCrashLogCleaner(AppSettingsDataStore.debugLogsMaxStoreAmountFlow.first())
                }
                crashSaveSuccess = true
            } finally {
                runBlocking {
                    if (AppSettingsDataStore.handleExceptionFlow.first()) {
                        if (isAppErrorCrash()) {
                            ApplicationInstance.appErrorRelaunch(if (crashSaveSuccess) crashFileName else null)
                        } else {
                            ApplicationInstance.crashRelaunch()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            defaultExceptionHandler?.uncaughtException(Thread.currentThread(), e)
        }
    }

    private suspend fun isAppErrorCrash(): Boolean {
        val lastCrashMills = CrashFileManager.readCrashRecord()
        val currentCrashMills = System.currentTimeMillis()
        CrashFileManager.writeCrashRecord(currentCrashMills)
        return currentCrashMills - lastCrashMills <= CRASH_RELAUNCH_PERIOD_MILLS
    }

    private fun getNewCrashFileName() = buildString {
        append(CRASH_LOG_FILE_PREFIX)
        append(CRASH_LOG_FILE_CONNECT_SYMBOL)
        append(BuildConfig.VERSION_NAME)
        append(CRASH_LOG_FILE_CONNECT_SYMBOL)
        append(System.currentTimeMillis())
        append(".")
        append(CRASH_LOG_FILE_EXTENSION)
    }

    private fun generateCrashLog(thread: Thread, throwable: Throwable) = buildString {
        appendLine(Date().toString())
        appendLine()
        appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Device Name: ${Build.BRAND}  ${Build.MODEL}")
        appendLine("Device ABI: ${Build.SUPPORTED_ABIS?.joinToString()}")
        appendLine("Android Version: ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})")
        appendLine()
        appendLine(thread)
        appendLine(throwable.stackTraceToString())
    }
}