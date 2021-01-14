package tool.xfy9326.schedule.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.db.utils.DBTypeConverter
import tool.xfy9326.schedule.json.beans.*

object JSONManager {
    private val JSON by lazy {
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }

    fun encode(vararg schedules: ScheduleJSONBundle): String? {
        try {
            val data = getParsableClass(schedules)
            return JSON.encodeToString(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun decode(jsonText: String): List<ScheduleJSONBundle>? {
        try {
            val data = JSON.decodeFromString<BackupWrapperJSON>(jsonText)
            return fromParsableClass(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getParsableClass(data: Array<out ScheduleJSONBundle>): BackupWrapperJSON {
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
                courses = jsonCourses
            ))
        }

        return BackupWrapperJSON(data = scheduleJsonList)
    }

    private fun fromParsableClass(data: BackupWrapperJSON): List<ScheduleJSONBundle> {
        val scheduleList = ArrayList<ScheduleJSONBundle>()

        for (scheduleJson in data.data) {
            val schedule = Schedule(
                name = scheduleJson.name,
                times = scheduleJson.times.map { it.toScheduleTime() }.toTypedArray(),
                color = scheduleJson.color
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
            scheduleList.add(ScheduleJSONBundle(schedule, courses.toTypedArray()))
        }

        return scheduleList
    }
}