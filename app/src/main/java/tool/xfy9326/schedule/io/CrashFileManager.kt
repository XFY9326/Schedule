@file:Suppress("BlockingMethodInNonBlockingContext")

package tool.xfy9326.schedule.io

import android.net.Uri
import lib.xfy9326.kit.asParentOf
import lib.xfy9326.kit.runOnlyResultIOJob
import lib.xfy9326.kit.runSafeIOJob
import lib.xfy9326.kit.runSimpleIOJob
import okio.sink
import okio.source
import tool.xfy9326.schedule.io.kt.useBuffer
import tool.xfy9326.schedule.tools.ExceptionHandler

object CrashFileManager {
    private const val FILE_NAME_CRASH_RECORD = "LastCrashMills.record"
    private val FILE_CRASH_RECORD = PathManager.LogDir.asParentOf(FILE_NAME_CRASH_RECORD)

    suspend fun readCrashLog(name: String) = runSafeIOJob {
        PathManager.LogDir.asParentOf(name).source().useBuffer {
            readUtf8()
        }
    }

    suspend fun writeCrashLog(name: String, content: String) = runOnlyResultIOJob {
        PathManager.LogDir.asParentOf(name).sink().useBuffer {
            writeUtf8(content)
        }
        true
    }

    suspend fun copyLogFile(name: String, uri: Uri) = FileManager.copyFileToUri(PathManager.LogDir.asParentOf(name), uri)

    suspend fun readCrashRecord(): Long = runSafeIOJob(0L) {
        FILE_CRASH_RECORD.source().useBuffer {
            readUtf8().toLong()
        }
    }

    suspend fun writeCrashRecord(mills: Long) = runOnlyResultIOJob {
        FILE_CRASH_RECORD.sink().useBuffer {
            writeUtf8(mills.toString())
        }
        true
    }

    suspend fun getAllDebugLogsName() = runSafeIOJob {
        PathManager.LogDir.listFiles { _, name ->
            name.endsWith(ExceptionHandler.CRASH_LOG_FILE_EXTENSION)
        }?.sortedByDescending {
            it.lastModified()
        }?.map { it.name }
    } ?: emptyList()

    suspend fun runCrashLogCleaner(cleanSize: Int) = runSimpleIOJob {
        PathManager.LogDir.listFiles { _, name ->
            name.endsWith(ExceptionHandler.CRASH_LOG_FILE_EXTENSION)
        }?.takeIf { it.size - 1 > cleanSize + 1 }?.sortedBy {
            it.lastModified()
        }?.let { files ->
            files.take(files.size - cleanSize).forEach {
                it.delete()
            }
        }
    }
}