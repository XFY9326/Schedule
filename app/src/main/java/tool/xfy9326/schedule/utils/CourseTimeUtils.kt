package tool.xfy9326.schedule.utils

import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.kt.getWeekDay
import java.util.*
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

    fun getDayInWeek(weekNum: Int, termStartCountDate: Date, firstDayOfWeek: WeekDay, hasWeekend: Boolean): Array<Day> {
        val calendar = CalendarUtils.getCalendar(CalendarUtils.getFirstDateInThisWeek(termStartCountDate, firstDayOfWeek)).apply {
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

    fun getMaxWeekNum(startDate: Date, endDate: Date, firstDayOfWeek: WeekDay) =
        CalendarUtils.getWeekPeriod(
            CalendarUtils.getFirstDateInThisWeek(startDate, firstDayOfWeek),
            CalendarUtils.getLastDateInThisWeek(endDate, firstDayOfWeek, true)
        )

    fun getTermEndDate(startDate: Date, firstDayOfWeek: WeekDay, weekNum: Int): Date {
        val weekCountStart = CalendarUtils.getFirstDateInThisWeek(startDate, firstDayOfWeek)
        val endWeekNum = CalendarUtils.getCalendar(weekCountStart, firstDayOfWeek, true).apply {
            add(Calendar.DATE, weekNum * 7 - 1)
        }.time
        return CalendarUtils.getLastDateInThisWeek(endWeekNum, firstDayOfWeek)
    }

    fun getRealClassTime(
        scheduleCalculateTimes: ScheduleCalculateTimes,
        weekNum: Int,
        classTime: ClassTime,
    ): Pair<Date, Date> {
        CalendarUtils.getCalendar(
            date = scheduleCalculateTimes.weekCountBeginning,
            firstDayOfWeek = scheduleCalculateTimes.weekStart,
            clearToDate = true
        ).apply {
            val dayOffset = (weekNum - 1) * 7 + classTime.weekDay.value(scheduleCalculateTimes.weekStart) - 1
            if (dayOffset != 0) add(Calendar.DATE, dayOffset)

            val start = scheduleCalculateTimes.times[classTime.classStartTime - 1]
            set(Calendar.HOUR_OF_DAY, start.startHour)
            set(Calendar.MINUTE, start.startMinute)
            val startTime = time

            val end = scheduleCalculateTimes.times[classTime.classEndTime - 1]
            set(Calendar.HOUR_OF_DAY, end.endHour)
            set(Calendar.MINUTE, end.endMinute)
            val endTime = time

            return startTime to endTime
        }
    }
}