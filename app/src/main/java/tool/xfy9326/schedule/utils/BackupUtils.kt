package tool.xfy9326.schedule.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.db.utils.DBTypeConverter
import tool.xfy9326.schedule.io.TextIO
import tool.xfy9326.schedule.json.*

object BackupUtils {
    private val JSON by lazy {
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }

    fun createBackupFileName(context: Context) = "${context.getString(R.string.app_name)}-${System.currentTimeMillis() / 1000}"

    suspend fun backupSchedules(uri: Uri, scheduleIds: List<Long>): Boolean {
        try {
            ScheduleDBProvider.db.scheduleDAO.apply {
                val allBundles = Array(scheduleIds.size) {
                    val schedule = getSchedule(scheduleIds[it]).first()!!
                    val courses = getScheduleCourses(scheduleIds[it]).first()
                    ScheduleCourseBundle(schedule, courses)
                }
                encode(*allBundles)?.let {
                    TextIO.writeText(it, uri)
                }
            }
            return true
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
            TextIO.readText(uri)?.let(::decode)?.let {
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

    private fun encode(vararg schedules: ScheduleCourseBundle): String? {
        try {
            val data = getParsableClass(schedules)
            return JSON.encodeToString(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun decode(jsonText: String): List<ScheduleCourseBundle>? {
        try {
            val data = JSON.decodeFromString<BackupWrapperJSON>(jsonText)
            return fromParsableClass(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getParsableClass(data: Array<out ScheduleCourseBundle>): BackupWrapperJSON {
        val scheduleJsonList = ArrayList<ScheduleJSON>()

        for (datum in data) {
            val jsonCourses = ArrayList<CourseJSON>(datum.courses.size)
            for (course in datum.courses) {
                val jsonCourseTimes = ArrayList<CourseTimeJSON>(course.times.size)
                for (time in course.times) {
                    jsonCourseTimes.add(CourseTimeJSON(
                        weekNum = DBTypeConverter.instance.booleanArrayToString(time.weekNum)!!,
                        weekDay = time.classTime.weekDay.shortName,
                        start = time.classTime.classStartTime,
                        duration = time.classTime.classDuration,
                        location = time.location
                    ))
                }
                jsonCourses.add(CourseJSON(
                    name = course.name,
                    teacher = course.teacher,
                    color = course.color,
                    times = jsonCourseTimes
                ))
            }
            scheduleJsonList.add(ScheduleJSON(
                name = datum.schedule.name,
                times = datum.schedule.times.map { ScheduleTimeJSON.fromScheduleTime(it) },
                color = datum.schedule.color,
                weekStart = datum.schedule.weekStart.shortName,
                courses = jsonCourses
            ))
        }

        return BackupWrapperJSON(data = scheduleJsonList)
    }

    private fun fromParsableClass(data: BackupWrapperJSON): List<ScheduleCourseBundle> {
        val scheduleList = ArrayList<ScheduleCourseBundle>()

        for (scheduleJson in data.data) {
            val schedule = Schedule(
                name = scheduleJson.name,
                times = scheduleJson.times.map { it.toScheduleTime() }.toTypedArray(),
                color = scheduleJson.color,
                weekStart = WeekDay.valueOfShortName(scheduleJson.weekStart)
            )
            val courses = ArrayList<Course>(scheduleJson.courses.size)
            for (courseJson in scheduleJson.courses) {
                val courseTimes = ArrayList<CourseTime>(courseJson.times.size)
                for (timeJson in courseJson.times) {
                    courseTimes.add(CourseTime(
                        weekNum = DBTypeConverter.instance.stringToBooleanArray(timeJson.weekNum)!!,
                        weekDay = WeekDay.valueOfShortName(timeJson.weekDay),
                        classStartTime = timeJson.start,
                        classDuration = timeJson.duration,
                        location = timeJson.location
                    ))
                }
                courses.add(Course(
                    name = courseJson.name,
                    teacher = courseJson.teacher,
                    color = courseJson.color,
                    times = courseTimes
                ))
            }
            scheduleList.add(ScheduleCourseBundle(schedule, courses.toTypedArray()))
        }

        return scheduleList
    }
}