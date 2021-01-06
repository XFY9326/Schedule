package tool.xfy9326.schedule.tools

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.io.TextIO
import tool.xfy9326.schedule.kt.appErrorRelaunch
import tool.xfy9326.schedule.kt.crashRelaunch
import tool.xfy9326.schedule.utils.DirUtils
import java.io.File
import java.util.*

object ExceptionHandler : Thread.UncaughtExceptionHandler {
    private const val CRASH_RELAUNCH_PERIOD_MILLS = 5000L
    private const val CRASH_LOG_FILE_EXTENSION = "log"
    private const val CRASH_LOG_FILE_PREFIX = "Crash"
    private const val CRASH_LOG_FILE_CONNECT_SYMBOL = "_"
    private const val CRASH_RECORD_FILE_NAME = "LastCrashMills.record"
    private val CRASH_RECORD_FILE = File(DirUtils.LogDir, CRASH_RECORD_FILE_NAME)

    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun init() {
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    suspend fun getAllDebugLogs() = withContext(Dispatchers.IO) {
        return@withContext DirUtils.LogDir.listFiles { _, name ->
            name.startsWith(CRASH_LOG_FILE_PREFIX) && name.endsWith(CRASH_LOG_FILE_EXTENSION)
        }?.sortedBy {
            it.lastModified()
        }?.map {
            it.name
        }?.toTypedArray() ?: emptyArray()
    }

    private suspend fun runCrashLogCleaner(cleanSize: Int) = withContext(Dispatchers.IO) {
        val files = DirUtils.LogDir.listFiles { _, name ->
            name.startsWith(CRASH_LOG_FILE_PREFIX) && name.endsWith(CRASH_LOG_FILE_EXTENSION)
        }
        if (files != null && files.size - 1 > cleanSize + 1) {
            files.sortedBy {
                it.lastModified()
            }.take(files.size - cleanSize).forEach {
                it.delete()
            }
        }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            val crashFileName = getNewCrashFileName()

            runBlocking {
                saveCrashDetail(crashFileName, e)
                runCrashLogCleaner(AppSettingsDataStore.debugLogsMaxStoreAmountFlow.first())

                if (AppSettingsDataStore.handleExceptionFlow.first()) {
                    if (isAppErrorCrash()) {
                        App.instance.appErrorRelaunch(crashFileName)
                    } else {
                        App.instance.crashRelaunch()
                    }
                }
            }
        } finally {
            defaultExceptionHandler?.uncaughtException(t, e)
        }
    }

    private suspend fun saveCrashMills(mills: Long) =
        TextIO.writeText(mills.toString(), CRASH_RECORD_FILE)

    private suspend fun readCrashMills(): Long =
        TextIO.readText(CRASH_RECORD_FILE)?.toLongOrNull() ?: 0

    private suspend fun isAppErrorCrash(): Boolean {
        val lastCrashMills = readCrashMills()
        val currentCrashMills = System.currentTimeMillis()
        saveCrashMills(currentCrashMills)
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

    private suspend fun saveCrashDetail(fileName: String, throwable: Throwable) =
        TextIO.writeText(generateCrashLog(throwable), File(DirUtils.LogDir, fileName))

    private fun generateCrashLog(throwable: Throwable) = buildString {
        appendLine(Date().toString())
        appendLine()
        appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Device Name: ${Build.BRAND}  ${Build.MODEL}")
        appendLine("Device ABI: ${Build.SUPPORTED_ABIS?.joinToString()}")
        appendLine("Android Version: ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})")
        appendLine()
        appendLine(throwable.stackTraceToString())
    }
}