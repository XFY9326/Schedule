package tool.xfy9326.schedule.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.first
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.BatchResult
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.io.TextIO
import tool.xfy9326.schedule.json.JSONManager
import tool.xfy9326.schedule.json.ScheduleJSONBundle

object BackupUtils {
    fun createBackupFileName(context: Context) = "${context.getString(R.string.app_name)}-${System.currentTimeMillis() / 1000}"

    suspend fun backupSchedules(context: Context, uri: Uri, scheduleIds: List<Long>): Boolean {
        try {
            ScheduleDBProvider.db.scheduleDAO.apply {
                val allBundles = Array(scheduleIds.size) {
                    val schedule = getSchedule(scheduleIds[it]).first()!!
                    val courses = getScheduleCourses(scheduleIds[it]).first()
                    ScheduleJSONBundle(schedule, courses)
                }
                JSONManager.encode(*allBundles)?.let {
                    TextIO.writeText(it, context, uri)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    suspend fun restoreSchedules(context: Context, uri: Uri): Pair<BatchResult, Boolean> {
        var totalAmount = 0
        var errorAmount = 0
        var hasConflicts = false
        try {
            TextIO.readText(context, uri)?.let(JSONManager::decode)?.let {
                for (bundle in it) {
                    totalAmount++
                    val scheduleTimeValid = ScheduleManager.validateScheduleTime(bundle.schedule.times)
                    if (!scheduleTimeValid) {
                        errorAmount++
                        continue
                    }
                    hasConflicts = CourseManager.solveConflicts(bundle.schedule.times, bundle.courses)
                    ScheduleManager.saveNewSchedule(bundle.schedule, bundle.courses)
                }
            }
            return BatchResult(true, totalAmount, errorAmount) to hasConflicts
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return BatchResult(false) to hasConflicts
    }


}