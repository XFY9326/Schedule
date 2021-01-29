package tool.xfy9326.schedule.utils

import android.content.Context
import android.net.Uri
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.beans.WeekNumPattern.PatternType.*
import tool.xfy9326.schedule.io.GlobalIO
import tool.xfy9326.schedule.io.TextIO
import tool.xfy9326.schedule.kt.iterateAll

class ScheduleICSHelper constructor(schedule: Schedule, private val courses: List<Course>) {
    companion object {
        fun createICSFileName(context: Context, scheduleName: String) = "${context.getString(R.string.app_name)}-$scheduleName"
    }

    private val scheduleCalculateTimes = ScheduleCalculateTimes(schedule)

    suspend fun dumpICS(uri: Uri): Boolean {
        try {
            val iCal = ScheduleICSWriter()
            courses.iterateAll { course, courseTime ->
                createCourseTimeVEvent(iCal, course, courseTime)
            }
            return TextIO.writeText(iCal.build(), uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun createCourseTimeVEvent(iCal: ScheduleICSWriter, course: Course, courseTime: CourseTime) {
        val weekNumPattern = WeekNumPattern(courseTime, scheduleCalculateTimes)
        if (weekNumPattern.type == SINGLE) {
            CourseTimeUtils.getRealClassTime(scheduleCalculateTimes, weekNumPattern.start + 1, courseTime.classTime).let { time ->
                iCal.addEvent(time.first, time.second, course.name, courseTime.location, getEventDescription(course))
            }
        } else if (weekNumPattern.type == SPACED || weekNumPattern.type == SERIAL) {
            CourseTimeUtils.getRealClassTime(scheduleCalculateTimes, weekNumPattern.start + 1, classTime = courseTime.classTime).let { time ->
                val rrule = ScheduleICSWriter.RRULE(weekNumPattern.interval,
                    weekNumPattern.amount,
                    courseTime.classTime.weekDay,
                    scheduleCalculateTimes.weekStart)
                iCal.addEvent(time.first, time.second, course.name, courseTime.location, getEventDescription(course), rrule)
            }
        } else if (weekNumPattern.type == MESSY) {
            for (period in weekNumPattern.timePeriodArray) {
                CourseTimeUtils.getRealClassTime(scheduleCalculateTimes, period.start + 1, courseTime.classTime).let { time ->
                    val rrule =
                        if (period.length > 1) {
                            ScheduleICSWriter.RRULE(1, period.length, courseTime.classTime.weekDay, scheduleCalculateTimes.weekStart)
                        } else {
                            null
                        }
                    iCal.addEvent(time.first, time.second, course.name, courseTime.location, getEventDescription(course), rrule)
                }
            }
        }
    }

    private fun getEventDescription(course: Course) =
        if (course.teacher == null) {
            null
        } else {
            GlobalIO.resources.getString(R.string.ics_description_teacher, course.teacher)
        }
}