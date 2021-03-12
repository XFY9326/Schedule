package tool.xfy9326.schedule.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import lib.xfy9326.io.processor.jsonReader
import lib.xfy9326.io.processor.jsonWriter
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.db.utils.DBTypeConverter
import tool.xfy9326.schedule.json.backup.*
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
            return uri.jsonWriter<BackupWrapperJSON>(JSON).write(getParsableClass(allBundles))
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
            uri.jsonReader<BackupWrapperJSON>(JSON).read()?.let {
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
            }
            return BatchResult(true, totalAmount, errorAmount) to hasConflicts
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
        val scheduleList = ArrayList<ScheduleCourseBundle>(data.data.size)

        for (scheduleJson in data.data) {
            val schedule = Schedule(
                name = scheduleJson.name,
                times = scheduleJson.times.map { it.toScheduleTime() },
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
            scheduleList.add(ScheduleCourseBundle(schedule, courses))
        }

        return scheduleList
    }
}