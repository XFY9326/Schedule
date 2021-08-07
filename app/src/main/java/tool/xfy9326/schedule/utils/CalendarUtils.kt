package tool.xfy9326.schedule.utils

import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.beans.WeekDay.Companion.calWeekDay
import tool.xfy9326.schedule.beans.WeekDay.Companion.getWeekDay
import tool.xfy9326.schedule.beans.WeekDay.Companion.orderedValue
import java.util.*
import kotlin.math.floor

object CalendarUtils {
    fun getCalendar(date: Date? = null, weekStart: WeekDay? = null, clearToDate: Boolean = false): Calendar =
        Calendar.getInstance(Locale.getDefault()).apply {
            if (date != null) time = date
            if (clearToDate) {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (weekStart != null) this.firstDayOfWeek = weekStart.calWeekDay
        }

    fun getDay(date: Date? = null): Day =
        getCalendar(date).let {
            Day(it.get(Calendar.MONTH) + 1, it.get(Calendar.DATE), it.getWeekDay())
        }

    fun getWeekDay(date: Date) = getCalendar(date).getWeekDay()

    fun getLastTimeOfDay(date: Date): Date =
        getCalendar(date).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

    fun getFirstDateInThisWeek(date: Date, weekStart: WeekDay): Date =
        getCalendar(date, weekStart, true).apply {
            add(Calendar.DATE, 1 - getWeekDay().orderedValue(weekStart))
        }.time

    fun getLastDateInThisWeek(date: Date, weekStart: WeekDay, actualEnd: Boolean = false): Date =
        getCalendar(date, weekStart, true).apply {
            add(Calendar.DATE, 7 - getWeekDay().orderedValue(weekStart) + (if (actualEnd) 1 else 0))
        }.time

    private fun clearToDay(date: Date): Date = getCalendar(date, clearToDate = true).time

    private fun getDayPeriod(startDate: Date, endDate: Date) =
        floor((clearToDay(endDate).time - clearToDay(startDate).time) / (24 * 60 * 60 * 1000f)).toInt()

    fun getWeekPeriod(startDate: Date, endDate: Date) =
        floor(getDayPeriod(startDate, endDate) / 7f).toInt()

    fun compareDateInDay(date1: Date, date2: Date): Int {
        val calendar1 = getCalendar(date1, clearToDate = true)
        val calendar2 = getCalendar(date2, clearToDate = true)
        return calendar1.compareTo(calendar2)
    }
}