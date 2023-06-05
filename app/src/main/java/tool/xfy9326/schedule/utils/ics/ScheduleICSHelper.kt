package tool.xfy9326.schedule.utils.ics

import android.content.Context
import android.net.Uri
import io.github.xfy9326.atools.io.IOManager
import io.github.xfy9326.atools.io.serialization.json.writeJSONAsync
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.Course.Companion.iterateAll
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleCalculateTimes
import tool.xfy9326.schedule.tools.NumberPattern.PatternType.EMPTY
import tool.xfy9326.schedule.tools.NumberPattern.PatternType.MESSY
import tool.xfy9326.schedule.tools.NumberPattern.PatternType.SERIAL
import tool.xfy9326.schedule.tools.NumberPattern.PatternType.SINGLE
import tool.xfy9326.schedule.tools.NumberPattern.PatternType.SPACED
import tool.xfy9326.schedule.utils.schedule.CourseTimeUtils
import tool.xfy9326.schedule.utils.schedule.WeekNumPattern

class ScheduleICSHelper constructor(schedule: Schedule, private val courses: List<Course>) {
    companion object {
        fun createICSFileName(context: Context, scheduleName: String) = "${context.getString(R.string.app_name)}-$scheduleName.ics"
    }

    private val scheduleCalculateTimes = ScheduleCalculateTimes(schedule)

    suspend fun dumpICS(uri: Uri): Boolean {
        try {
            val iCal = ScheduleICSWriter()
            courses.iterateAll { course, courseTime ->
                createCourseTimeVEvent(iCal, course, courseTime)
            }
            return uri.writeJSONAsync(iCal.build()).isSuccess
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun createCourseTimeVEvent(iCal: ScheduleICSWriter, course: Course, courseTime: CourseTime) {
        val weekNumPattern = WeekNumPattern.parsePattern(courseTime, scheduleCalculateTimes)
        when (weekNumPattern.type) {
            SINGLE -> {
                CourseTimeUtils.getRealSectionTime(scheduleCalculateTimes, weekNumPattern.start + 1, courseTime.sectionTime).let { time ->
                    iCal.addEvent(time.first, time.second, course.name, courseTime.location, getEventDescription(course))
                }
            }

            SPACED, SERIAL -> {
                CourseTimeUtils.getRealSectionTime(scheduleCalculateTimes, weekNumPattern.start + 1, sectionTime = courseTime.sectionTime)
                    .let { time ->
                        val rrule = ScheduleICSWriter.RRULE(
                            weekNumPattern.interval,
                            weekNumPattern.amount,
                            courseTime.sectionTime.weekDay,
                            scheduleCalculateTimes.weekStart
                        )
                        iCal.addEvent(time.first, time.second, course.name, courseTime.location, getEventDescription(course), rrule)
                    }
            }

            MESSY -> {
                for (period in weekNumPattern.timePeriodArray) {
                    CourseTimeUtils.getRealSectionTime(scheduleCalculateTimes, period.start + 1, courseTime.sectionTime).let { time ->
                        val rrule =
                            if (period.length > 1) {
                                ScheduleICSWriter.RRULE(1, period.length, courseTime.sectionTime.weekDay, scheduleCalculateTimes.weekStart)
                            } else {
                                null
                            }
                        iCal.addEvent(time.first, time.second, course.name, courseTime.location, getEventDescription(course), rrule)
                    }
                }
            }

            EMPTY -> Unit
        }
    }

    private fun getEventDescription(course: Course) =
        if (course.teacher == null) {
            null
        } else {
            IOManager.resources.getString(R.string.ics_description_teacher, course.teacher)
        }
}