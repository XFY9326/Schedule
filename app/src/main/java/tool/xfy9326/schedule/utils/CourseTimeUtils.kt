package tool.xfy9326.schedule.utils

import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.kt.getWeekDay
import java.util.*
import kotlin.math.ceil

object CourseTimeUtils {
    fun getWeekNum(schedule: Schedule, firstDayOfWeek: WeekDay, calculateDate: Date = Date()): Int {
        if (CalendarUtils.compareDateInDay(calculateDate, schedule.startDate) < 0) {
            return 0
        }
        if (CalendarUtils.compareDateInDay(calculateDate, schedule.endDate) > 0) {
            return 0
        }
        val fixedStartDate = CalendarUtils.getFirstDateInThisWeek(schedule.startDate, firstDayOfWeek)
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

    fun getMaxWeekNum(startDate: Date, endDate: Date) =
        CalendarUtils.getWeekPeriod(
            CalendarUtils.getFirstDateInThisWeek(startDate, WeekDay.MONDAY),
            CalendarUtils.getLastDateInThisWeek(endDate, WeekDay.MONDAY, true)
        )

    fun getTermEndDate(startDate: Date, firstDayOfWeek: WeekDay, weekNum: Int): Date =
        CalendarUtils.getFirstDateInThisWeek(startDate, firstDayOfWeek).apply {
            if (weekNum > 0) time += 7 * 24 * 60 * 60 * 1000 * weekNum - 24 * 60 * 60 * 1000
        }
}