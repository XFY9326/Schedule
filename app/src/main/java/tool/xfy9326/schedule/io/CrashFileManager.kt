@file:Suppress("BlockingMethodInNonBlockingContext")

package tool.xfy9326.schedule.io

import android.net.Uri
import io.github.xfy9326.atools.io.okio.copyTo
import io.github.xfy9326.atools.io.okio.useBuffer
import io.github.xfy9326.atools.io.utils.asParentOf
import io.github.xfy9326.atools.io.utils.runIOJob
import okio.sink
import okio.source
import tool.xfy9326.schedule.tools.ExceptionHandler

object CrashFileManager {
    private const val FILE_NAME_CRASH_RECORD = "LastCrashRecord"
    private val FILE_CRASH_RECORD = PathManager.LogDir.asParentOf(FILE_NAME_CRASH_RECORD)

    suspend fun readCrashLog(name: String): String? = runIOJob {
        PathManager.LogDir.asParentOf(name).source().useBuffer {
            readUtf8()
        }
    }.getOrNull()

    suspend fun writeCrashLog(name: String, content: String): Boolean = runIOJob {
        PathManager.LogDir.asParentOf(name).sink().useBuffer {
            writeUtf8(content)
        }
    }.isSuccess

    suspend fun copyLogFile(name: String, uri: Uri): Boolean = runIOJob {
        PathManager.LogDir.asParentOf(name).copyTo(uri)
    }.isSuccess

    suspend fun readCrashRecord(): Pair<Long, Boolean> = runIOJob {
        val contentArr = FILE_CRASH_RECORD.source().useBuffer {
            readUtf8().split(",")
        }
        contentArr[0].toLong() to contentArr[1].toBooleanStrict()
    }.getOrNull() ?: 0L to false

    suspend fun writeCrashRecord(mills: Long, isAppErrorRelaunch: Boolean): Boolean = runIOJob {
        FILE_CRASH_RECORD.sink().useBuffer {
            writeUtf8("$mills,$isAppErrorRelaunch")
        }
    }.isSuccess

    suspend fun getAllDebugLogsName(): List<String> = runIOJob {
        PathManager.LogDir.listFiles { _, name ->
            name.endsWith(ExceptionHandler.CRASH_LOG_FILE_EXTENSION)
        }?.sortedByDescending {
            it.lastModified()
        }?.map { it.name }
    }.getOrNull() ?: emptyList()

    suspend fun runCrashLogCleaner(cleanSize: Int): Boolean = runIOJob {
        PathManager.LogDir.listFiles { _, name ->
            name.endsWith(ExceptionHandler.CRASH_LOG_FILE_EXTENSION)
        }?.takeIf { it.size - 1 > cleanSize + 1 }?.sortedBy {
            it.lastModified()
        }?.let { files ->
            files.take(files.size - cleanSize).forEach {
                it.delete()
            }
        }
    }.isSuccess
}