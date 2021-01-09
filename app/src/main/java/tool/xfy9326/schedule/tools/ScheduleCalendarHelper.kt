package tool.xfy9326.schedule.tools

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
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.*

class ScheduleCalendarHelper constructor(schedule: Schedule, private val courses: Array<Course>, private val firstDayOfWeek: WeekDay) {
    companion object {
        private const val CAL_ID = "Schedule"
        private const val PRODID = "-//Produced By: $CAL_ID//App Version: ${BuildConfig.VERSION_CODE}"

        fun createICSFileName(scheduleName: String) = "$CAL_ID-$scheduleName"
    }

    private val scheduleStartTime = schedule.startDate
    private val actualScheduleEndTime = CalendarUtils.getLastTimeOfDay(schedule.endDate)
    private val scheduleWeekCountBeginning = CalendarUtils.getFirstDateInThisWeek(schedule.startDate, firstDayOfWeek)
    private val scheduleTimes = schedule.times

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
        val weekNumPattern = courseTime.weekNumPattern
        if (weekNumPattern.type == SINGLE) getCourseFirstAvailableTime(weekNumPattern.start, classTime = courseTime.classTime)?.let { time ->
            iCal.addEvent(VEvent().apply {
                setDateStart(time.first)
                setDateEnd(time.second)
                addBasicInfoToVEvent(this, course, courseTime)
            })
        } else if (weekNumPattern.type == SPACED || weekNumPattern.type == SERIAL) getCourseFirstAvailableTime(
            weekNumPattern.start,
            weekNumPattern.end,
            weekNumPattern.interval,
            classTime = courseTime.classTime
        )?.let { time ->
            iCal.addEvent(VEvent().apply {
                setDateStart(time.first)
                setDateEnd(time.second)
                setRecurrenceRule(Recurrence.Builder(Frequency.WEEKLY).apply {
                    interval(weekNumPattern.interval)
                    count(weekNumPattern.amount)
                    byDay(getDayOfWeek(courseTime.classTime.weekDay))
                    workweekStarts(getDayOfWeek(firstDayOfWeek))
                }.build())
                addBasicInfoToVEvent(this, course, courseTime)
            })
        } else if (weekNumPattern.type == MESSY) for (period in weekNumPattern.timePeriodArray) {
            getCourseFirstAvailableTime(period.start, period.end, classTime = courseTime.classTime)?.let { time ->
                iCal.addEvent(VEvent().apply {
                    setDateStart(time.first)
                    setDateEnd(time.second)
                    if (period.length > 1) {
                        setRecurrenceRule(Recurrence.Builder(Frequency.WEEKLY).apply {
                            interval(1)
                            count(period.length)
                            byDay(getDayOfWeek(courseTime.classTime.weekDay))
                            workweekStarts(getDayOfWeek(firstDayOfWeek))
                        }.build())
                    }
                    addBasicInfoToVEvent(this, course, courseTime)
                })
            }
        }
    }

    private fun addBasicInfoToVEvent(event: VEvent, course: Course, courseTime: CourseTime) {
        event.apply {
            setUid("$CAL_ID-${Uid.random().value}")
            setSummary(course.name)
            courseTime.location?.let {
                setLocation(it)
            }
            course.teacher?.let {
                setDescription(App.instance.getString(R.string.ics_description_teacher, it))
            }
        }
    }

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

    private fun getCourseFirstAvailableTime(
        start: Int,
        end: Int = start,
        interval: Int = 1,
        classTime: ClassTime,
    ): Pair<Date, Date>? {
        for (i in start..end step interval) {
            val temp = getRealClassTime(i, classTime)
            if (temp != null) return temp
        }
        return null
    }

    private fun getRealClassTime(weekNum: Int, classTime: ClassTime): Pair<Date, Date>? {
        CalendarUtils.getCalendar(date = scheduleWeekCountBeginning, firstDayOfWeek = firstDayOfWeek, clearToDate = true).apply {
            val dayOffset = (weekNum - 1) * 7 + classTime.weekDay.value(this@ScheduleCalendarHelper.firstDayOfWeek) - 1
            if (dayOffset != 0) add(Calendar.DATE, dayOffset)

            val start = scheduleTimes[classTime.classStartTime - 1]
            set(Calendar.HOUR_OF_DAY, start.startHour)
            set(Calendar.MINUTE, start.startMinute)
            val startTime = time

            val end = scheduleTimes[classTime.classEndTime - 1]
            set(Calendar.HOUR_OF_DAY, end.endHour)
            set(Calendar.MINUTE, end.endMinute)
            val endTime = time

            if (startTime < scheduleStartTime || endTime > actualScheduleEndTime) return null

            return startTime to time
        }
    }
}