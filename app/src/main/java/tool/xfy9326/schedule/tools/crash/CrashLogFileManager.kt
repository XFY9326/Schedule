package tool.xfy9326.schedule.tools.crash

import java.io.File
import java.io.IOException
import java.util.Properties

internal object CrashLogFileManager {
    private const val PROPERTIES_LAST_CRASH_MILLS = "last_crash_mills"
    private const val PROPERTIES_LAST_CRASH_LOG_FILE = "last_crash_log_file"
    private const val FILE_LAST_CRASH_PROPERTIES = "last_crash.properties"
    private const val FILE_PREFIX_CRASH = "Crash_"
    private const val FILE_EXTENSION_LOG = ".log"

    @Throws(IOException::class)
    fun listLogFiles(logDir: File): List<File> =
        logDir.takeIf { it.isDirectory }?.listFiles { _, name ->
            name.startsWith(FILE_PREFIX_CRASH) && name.endsWith(FILE_EXTENSION_LOG)
        }?.sortedByDescending {
            it.lastModified()
        } ?: emptyList()

    @Throws(IOException::class)
    fun cleanOldLog(logDir: File, maxLogAmount: Int) {
        logDir.takeIf { it.isDirectory }?.listFiles { _, name ->
            name.startsWith(FILE_PREFIX_CRASH) && name.endsWith(FILE_EXTENSION_LOG)
        }?.takeIf { it.size - 1 > maxLogAmount + 1 }?.sortedBy {
            it.lastModified()
        }?.let { files ->
            files.take(files.size - maxLogAmount).forEach {
                it.delete()
            }
        }
    }

    @Throws(IOException::class)
    fun logCrash(crashLogDir: File, versionName: String, crashLog: String): File {
        val mills = System.currentTimeMillis()
        val crashLogFile = File(crashLogDir, "$FILE_PREFIX_CRASH${versionName}_$mills$FILE_EXTENSION_LOG")
        if (crashLogDir.let { it.exists() || it.mkdirs() }) {
            crashLogFile.writeText(crashLog)
            saveLastCrashInfo(crashLogDir, mills, crashLogFile)
        } else {
            throw FileSystemException(file = crashLogDir, reason = "Failed to create directory.")
        }
        return crashLogFile
    }

    @Throws(IOException::class)
    private fun saveLastCrashInfo(crashLogDir: File, crashMills: Long, crashLogFile: File) {
        saveCrashProperties(crashLogDir, Properties().also {
            it[PROPERTIES_LAST_CRASH_MILLS] = crashMills.toString()
            it[PROPERTIES_LAST_CRASH_LOG_FILE] = crashLogFile.absolutePath
        })
    }

    @Throws(IOException::class)
    fun readLastCrashInfo(crashLogDir: File): LastCrashInfo {
        return readCrashProperties(crashLogDir).let {
            LastCrashInfo(
                lastCrashMills = it[PROPERTIES_LAST_CRASH_MILLS]?.toString()?.toLongOrNull() ?: 0,
                lastCrashLogPath = it[PROPERTIES_LAST_CRASH_LOG_FILE]?.toString()
            )
        }
    }

    @Throws(IOException::class)
    private fun saveCrashProperties(crashLogDir: File, properties: Properties) {
        val crashLogMeta = File(crashLogDir, FILE_LAST_CRASH_PROPERTIES)
        if (crashLogDir.let { it.exists() || it.mkdirs() }) {
            properties.store(crashLogMeta.outputStream(), "Crash Log Properties")
        } else {
            throw FileSystemException(file = crashLogDir, reason = "Failed to create directory.")
        }
    }

    @Throws(IOException::class)
    private fun readCrashProperties(crashLogDir: File): Properties {
        val crashLogMeta = File(crashLogDir, FILE_LAST_CRASH_PROPERTIES)
        val properties = Properties()
        if (crashLogMeta.isFile) {
            properties.load(crashLogMeta.inputStream())
        }
        return properties
    }
}