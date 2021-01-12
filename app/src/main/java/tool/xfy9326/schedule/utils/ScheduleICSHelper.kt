package tool.xfy9326.schedule.utils

import android.content.Context
import android.net.Uri
import biweekly.ICalendar
import biweekly.component.VEvent
import biweekly.property.CalendarScale
import biweekly.property.ProductId
import biweekly.property.Uid
import biweekly.util.DayOfWeek
import biweekly.util.Frequency
import biweekly.util.Recurrence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.beans.WeekDay.*
import tool.xfy9326.schedule.beans.WeekNumPattern.PatternType.*
import tool.xfy9326.schedule.kt.iterateAll
import java.util.*

class ScheduleICSHelper constructor(schedule: Schedule, private val courses: Array<Course>, private val firstDayOfWeek: WeekDay) {
    companion object {
        private const val CAL_ID = "Schedule"
        private const val PRODID = "-//Produced By: $CAL_ID//App Version: ${BuildConfig.VERSION_CODE}"

        fun createICSFileName(scheduleName: String) = "$CAL_ID-$scheduleName"

        private fun createWeeklyRRULE(interval: Int, count: Int, weekDay: WeekDay, firstDayOfWeek: WeekDay) =
            Recurrence.Builder(Frequency.WEEKLY).apply {
                interval(interval)
                count(count)
                byDay(getDayOfWeek(weekDay))
                workweekStarts(getDayOfWeek(firstDayOfWeek))
            }.build()

        private fun getDayOfWeek(weekDay: WeekDay) =
            when (weekDay) {
                MONDAY -> DayOfWeek.MONDAY
                TUESDAY -> DayOfWeek.TUESDAY
                WEDNESDAY -> DayOfWeek.WEDNESDAY
                THURSDAY -> DayOfWeek.THURSDAY
                FRIDAY -> DayOfWeek.FRIDAY
                SATURDAY -> DayOfWeek.SATURDAY
                SUNDAY -> DayOfWeek.SUNDAY
            }
    }

    private val scheduleCalculateTimes = ScheduleCalculateTimes(schedule, firstDayOfWeek)

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun dumpICS(context: Context, uri: Uri): Boolean {
        try {
            val iCal = ICalendar().apply {
                productId = ProductId(PRODID)
                calendarScale = CalendarScale.gregorian()
            }
            courses.iterateAll { course, courseTime ->
                createCourseTimeVEvent(iCal, course, courseTime)
            }
            withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(uri).use {
                    iCal.write(it)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun createCourseTimeVEvent(iCal: ICalendar, course: Course, courseTime: CourseTime) {
        val weekNumPattern = WeekNumPattern(courseTime, scheduleCalculateTimes)
        if (weekNumPattern.type == SINGLE) {
            CourseTimeUtils.getRealClassTime(scheduleCalculateTimes, weekNumPattern.start + 1, courseTime.classTime).let { time ->
                iCal.addEvent(VEvent().apply {
                    addBasicInfoToVEvent(this, time, course, courseTime)
                })
            }
        } else if (weekNumPattern.type == SPACED || weekNumPattern.type == SERIAL) {
            CourseTimeUtils.getRealClassTime(scheduleCalculateTimes, weekNumPattern.start + 1, classTime = courseTime.classTime).let { time ->
                iCal.addEvent(VEvent().apply {
                    setRecurrenceRule(createWeeklyRRULE(weekNumPattern.interval, weekNumPattern.amount, courseTime.classTime.weekDay, firstDayOfWeek))
                    addBasicInfoToVEvent(this, time, course, courseTime)
                })
            }
        } else if (weekNumPattern.type == MESSY) {
            for (period in weekNumPattern.timePeriodArray) {
                CourseTimeUtils.getRealClassTime(scheduleCalculateTimes, period.start + 1, courseTime.classTime).let { time ->
                    iCal.addEvent(VEvent().apply {
                        if (period.length > 1) {
                            setRecurrenceRule(createWeeklyRRULE(1, period.length, courseTime.classTime.weekDay, firstDayOfWeek))
                        }
                        addBasicInfoToVEvent(this, time, course, courseTime)
                    })
                }
            }
        }
    }

    private fun addBasicInfoToVEvent(event: VEvent, time: Pair<Date, Date>, course: Course, courseTime: CourseTime) {
        event.apply {
            setUid("$CAL_ID-${Uid.random().value}")
            setSummary(course.name)
            courseTime.location?.let {
                setLocation(it)
            }
            course.teacher?.let {
                setDescription(App.instance.getString(R.string.ics_description_teacher, it))
            }
            setDateStart(time.first)
            setDateEnd(time.second)
        }
    }
}