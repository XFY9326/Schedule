package tool.xfy9326.schedule.utils.schedule

import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleCalculateTimes
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.SectionTime
import tool.xfy9326.schedule.beans.SectionTime.Companion.end
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.beans.WeekDay.Companion.getWeekDay
import tool.xfy9326.schedule.beans.WeekDay.Companion.isWeekend
import tool.xfy9326.schedule.beans.WeekDay.Companion.orderedValue
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.Calendar
import java.util.Date
import kotlin.math.ceil

object CourseTimeUtils {
    fun getWeekNum(schedule: Schedule, calculateDate: Date = Date()): Int {
        if (CalendarUtils.compareDateInDay(calculateDate, schedule.startDate) < 0) {
            return 0
        }
        if (CalendarUtils.compareDateInDay(calculateDate, schedule.endDate) > 0) {
            return 0
        }
        val fixedStartDate = CalendarUtils.getFirstDateInThisWeek(schedule.startDate, schedule.weekStart)
        return ceil((calculateDate.time - fixedStartDate.time) / (7 * 24 * 60 * 60 * 1000f)).toInt()
    }

    fun getDayInWeek(weekNum: Int, termStartCountDate: Date, weekStart: WeekDay, hasWeekend: Boolean): Array<Day> {
        val calendar = CalendarUtils.getCalendar(CalendarUtils.getFirstDateInThisWeek(termStartCountDate, weekStart)).apply {
            val dayOffset = (weekNum - 1) * 7 - 1
            if (dayOffset != 0) add(Calendar.DATE, dayOffset)
        }
        return Array(if (hasWeekend) WeekDay.MAX_VALUE else WeekDay.MAX_VALUE - 2) {
            calendar.add(Calendar.DATE, 1)
            while (!hasWeekend && calendar.getWeekDay().isWeekend) calendar.add(Calendar.DATE, 1)
            Day(
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DATE),
                calendar.getWeekDay()
            )
        }
    }

    fun getMaxWeekNum(startDate: Date, endDate: Date, weekStart: WeekDay): Int {
        val fixedStartDate = CalendarUtils.getFirstDateInThisWeek(startDate, weekStart)
        val fixedEndDate = CalendarUtils.getLastDateInThisWeek(endDate, weekStart, true)
        if (fixedStartDate >= fixedEndDate) return 0
        return CalendarUtils.getWeekPeriod(fixedStartDate, fixedEndDate)
    }

    fun getTermEndDate(startDate: Date, weekStart: WeekDay, weekNum: Int): Date {
        val fixedStartDate = CalendarUtils.getFirstDateInThisWeek(startDate, weekStart)
        val endDate = CalendarUtils.getCalendar(fixedStartDate, weekStart, true).apply {
            add(Calendar.DATE, weekNum * 7 - 1)
        }.time
        return CalendarUtils.getLastDateInThisWeek(endDate, weekStart)
    }

    fun getMaxTermEndDate(startDate: Date, weekStart: WeekDay): Date =
        getTermEndDate(startDate, weekStart, ScheduleUtils.MAX_WEEK_NUM)

    fun calculateTermEndDate(startDate: Date, maxWeekNum: Int, weekStart: WeekDay): Date {
        val fixedStartDate = CalendarUtils.getFirstDateInThisWeek(startDate, weekStart)
        val endDate = CalendarUtils.getCalendar(fixedStartDate, weekStart, true).apply {
            add(Calendar.DATE, 7 * maxWeekNum - 1)
        }.time
        return CalendarUtils.getLastDateInThisWeek(endDate, weekStart)
    }

    fun getCourseSectionEndTime(date: Date, scheduleTimes: List<ScheduleTime>, sectionTime: SectionTime): Long =
        CalendarUtils.getCalendar(date = date, clearToDate = true).apply {
            val scheduleTime = scheduleTimes[sectionTime.end - 1]
            set(Calendar.HOUR_OF_DAY, scheduleTime.endHour)
            set(Calendar.MINUTE, scheduleTime.endMinute)
        }.timeInMillis

    fun getRealSectionTime(
        scheduleCalculateTimes: ScheduleCalculateTimes,
        weekNum: Int,
        sectionTime: SectionTime,
    ): Pair<Date, Date> {
        CalendarUtils.getCalendar(
            date = scheduleCalculateTimes.weekCountBeginning,
            weekStart = scheduleCalculateTimes.weekStart,
            clearToDate = true
        ).apply {
            val dayOffset = (weekNum - 1) * 7 + sectionTime.weekDay.orderedValue(scheduleCalculateTimes.weekStart) - 1
            if (dayOffset != 0) add(Calendar.DATE, dayOffset)

            val start = scheduleCalculateTimes.times[sectionTime.start - 1]
            set(Calendar.HOUR_OF_DAY, start.startHour)
            set(Calendar.MINUTE, start.startMinute)
            val startTime = time

            val end = scheduleCalculateTimes.times[sectionTime.end - 1]
            set(Calendar.HOUR_OF_DAY, end.endHour)
            set(Calendar.MINUTE, end.endMinute)
            val endTime = time

            return startTime to endTime
        }
    }
}