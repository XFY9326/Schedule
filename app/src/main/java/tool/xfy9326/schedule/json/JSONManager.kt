package tool.xfy9326.schedule.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.db.utils.DBTypeConverter
import tool.xfy9326.schedule.json.beans.CourseJson
import tool.xfy9326.schedule.json.beans.CourseTimeJson
import tool.xfy9326.schedule.json.beans.ScheduleJson
import tool.xfy9326.schedule.json.beans.ScheduleTimeJson

object JSONManager {
    private val JSON by lazy {
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }

    fun encode(vararg schedules: ScheduleJsonBundle): String? {
        try {
            val data = getParsableClass(schedules)
            return JSON.encodeToString(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun decode(jsonText: String): List<ScheduleJsonBundle>? {
        try {
            val data = JSON.decodeFromString<List<ScheduleJson>>(jsonText)
            return fromParsableClass(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getParsableClass(data: Array<out ScheduleJsonBundle>): List<ScheduleJson> {
        val scheduleJsonList = ArrayList<ScheduleJson>()

        for (datum in data) {
            val jsonCourses = ArrayList<CourseJson>(datum.courses.size)
            for (course in datum.courses) {
                val jsonCourseTimes = ArrayList<CourseTimeJson>(course.times.size)
                for (time in course.times) {
                    jsonCourseTimes.add(CourseTimeJson(
                        weekNum = DBTypeConverter.instance.booleanArrayToString(time.weekNum)!!,
                        weekDay = time.classTime.weekDay.shortName,
                        start = time.classTime.classStartTime,
                        duration = time.classTime.classDuration,
                        location = time.location
                    ))
                }
                jsonCourses.add(CourseJson(
                    name = course.name,
                    teacher = course.teacher,
                    color = course.color,
                    times = jsonCourseTimes
                ))
            }
            scheduleJsonList.add(ScheduleJson(
                name = datum.schedule.name,
                times = datum.schedule.times.map { ScheduleTimeJson.fromScheduleTime(it) },
                color = datum.schedule.color,
                courses = jsonCourses
            ))
        }

        return scheduleJsonList
    }

    private fun fromParsableClass(data: List<ScheduleJson>): List<ScheduleJsonBundle> {
        val scheduleList = ArrayList<ScheduleJsonBundle>()

        for (scheduleJson in data) {
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
            scheduleList.add(ScheduleJsonBundle(schedule, courses.toTypedArray()))
        }

        return scheduleList
    }
}