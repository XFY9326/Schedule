package tool.xfy9326.schedule.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.io.utils.readJSON
import tool.xfy9326.schedule.io.utils.writeJSON
import tool.xfy9326.schedule.json.ScheduleTimeJSON
import tool.xfy9326.schedule.json.backup.BackupWrapperJSON
import tool.xfy9326.schedule.json.backup.CourseJSON
import tool.xfy9326.schedule.json.backup.CourseTimeJSON
import tool.xfy9326.schedule.json.backup.ScheduleJSON
import tool.xfy9326.schedule.utils.schedule.CourseUtils
import tool.xfy9326.schedule.utils.schedule.ScheduleUtils

object BackupUtils {
    private val JSON by lazy {
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }

    fun createBackupFileName(context: Context) = "${context.getString(R.string.app_name)}-${System.currentTimeMillis() / 1000}.json"

    suspend fun backupSchedules(uri: Uri, scheduleIds: List<Long>): Boolean {
        try {
            val allBundles = ScheduleDBProvider.db.scheduleDAO.run {
                Array(scheduleIds.size) {
                    val schedule = getSchedule(scheduleIds[it]).first()!!
                    val courses = getScheduleCourses(scheduleIds[it]).first()
                    ScheduleCourseBundle(schedule, courses)
                }
            }
            return uri.writeJSON(getParsableClass(allBundles), JSON)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    suspend fun restoreSchedules(uri: Uri): Pair<BatchResult, Boolean> {
        var totalAmount = 0
        var errorAmount = 0
        var hasConflicts = false
        try {
            uri.readJSON<BackupWrapperJSON>(JSON)?.let {
                val originalData = fromParsableClass(it)
                for (bundle in originalData) {
                    totalAmount++
                    val scheduleTimeValid = ScheduleUtils.validateScheduleTime(bundle.schedule.times)
                    if (!scheduleTimeValid) {
                        errorAmount++
                        continue
                    }
                    hasConflicts = CourseUtils.solveConflicts(bundle.schedule.times, bundle.courses)
                    ScheduleUtils.saveNewSchedule(bundle.schedule, bundle.courses)
                }
                return BatchResult(true, totalAmount, errorAmount) to hasConflicts
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return BatchResult(false) to hasConflicts
    }

    private fun getParsableClass(data: Array<out ScheduleCourseBundle>): BackupWrapperJSON {
        val scheduleJsonList = ArrayList<ScheduleJSON>(data.size)

        for (datum in data) {
            val jsonCourses = ArrayList<CourseJSON>(datum.courses.size)
            for (course in datum.courses) {
                val jsonCourseTimes = ArrayList<CourseTimeJSON>(course.times.size)
                for (time in course.times) {
                    jsonCourseTimes.add(
                        CourseTimeJSON(
                            weekNum = time.weekNumArray,
                            weekDay = time.sectionTime.weekDay,
                            start = time.sectionTime.start,
                            duration = time.sectionTime.duration,
                            location = time.location
                        )
                    )
                }
                jsonCourses.add(
                    CourseJSON(
                        name = course.name,
                        teacher = course.teacher,
                        color = course.color,
                        times = jsonCourseTimes
                    )
                )
            }
            scheduleJsonList.add(
                ScheduleJSON(
                    name = datum.schedule.name,
                    times = datum.schedule.times.map { ScheduleTimeJSON.fromScheduleTime(it) },
                    color = datum.schedule.color,
                    weekStart = datum.schedule.weekStart,
                    startDate = datum.schedule.startDate,
                    endDate = datum.schedule.endDate,
                    courses = jsonCourses
                )
            )
        }

        return BackupWrapperJSON(data = scheduleJsonList)
    }

    private fun fromParsableClass(data: BackupWrapperJSON): List<ScheduleCourseBundle> {
        val scheduleList = ArrayList<ScheduleCourseBundle>(data.data.size)

        for (scheduleJson in data.data) {
            val scheduleDatePair = getFixedScheduleDate(scheduleJson)
            val schedule = Schedule(
                name = scheduleJson.name,
                times = scheduleJson.times.map { it.toScheduleTime() },
                color = scheduleJson.color,
                weekStart = scheduleJson.weekStart,
                startDate = scheduleDatePair.first,
                endDate = scheduleDatePair.second,
            )
            val courses = ArrayList<Course>(scheduleJson.courses.size)
            for (courseJson in scheduleJson.courses) {
                val courseTimes = ArrayList<CourseTime>(courseJson.times.size)
                for (timeJson in courseJson.times) {
                    courseTimes.add(
                        CourseTime(
                            weekNum = timeJson.weekNum,
                            weekDay = timeJson.weekDay,
                            start = timeJson.start,
                            duration = timeJson.duration,
                            location = timeJson.location
                        )
                    )
                }
                courses.add(
                    Course(
                        name = courseJson.name,
                        teacher = courseJson.teacher,
                        color = courseJson.color,
                        times = courseTimes
                    )
                )
            }
            scheduleList.add(ScheduleCourseBundle(schedule, courses))
        }

        return scheduleList
    }

    private fun getFixedScheduleDate(scheduleJson: ScheduleJSON) =
        if (scheduleJson.startDate == null || scheduleJson.endDate == null || scheduleJson.startDate >= scheduleJson.endDate) {
            ScheduleUtils.getDefaultTermDate(scheduleJson.weekStart)
        } else {
            scheduleJson.startDate to scheduleJson.endDate
        }
}