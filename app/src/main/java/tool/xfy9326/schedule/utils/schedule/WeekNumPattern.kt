package tool.xfy9326.schedule.utils.schedule

import android.content.Context
import lib.xfy9326.kit.isEven
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.ScheduleCalculateTimes
import tool.xfy9326.schedule.beans.WeekDay.Companion.orderedValue
import tool.xfy9326.schedule.tools.NumberPattern
import tool.xfy9326.schedule.tools.NumberPattern.PatternType.*
import tool.xfy9326.schedule.utils.CalendarUtils

object WeekNumPattern {

    fun parsePattern(courseTime: CourseTime, scheduleCalculateTimes: ScheduleCalculateTimes): NumberPattern {
        // 根据课表学期时间修正该周是否需要上课
        val tempWeekNum = courseTime.weekNum
        val maxWeekNum = scheduleCalculateTimes.maxWeekNum
        val weekStart = scheduleCalculateTimes.weekStart

        if (tempWeekNum.first()) {
            val startWeekDay = CalendarUtils.getWeekDay(scheduleCalculateTimes.actualStartTime)
            tempWeekNum[0] = startWeekDay.orderedValue(weekStart) <= courseTime.classTime.weekDay.orderedValue(weekStart)
        } else if (tempWeekNum.size == maxWeekNum && tempWeekNum.last()) {
            val endWeekDay = CalendarUtils.getWeekDay(scheduleCalculateTimes.actualEndTime)
            tempWeekNum[tempWeekNum.lastIndex] = endWeekDay.orderedValue(weekStart) >= courseTime.classTime.weekDay.orderedValue(weekStart)
        }

        return NumberPattern(tempWeekNum)
    }

    fun NumberPattern.getWeeksDescriptionText(context: Context) =
        when {
            type == EMPTY -> ""
            type == SINGLE -> (start + 1).toString()
            type == SERIAL -> "${start + 1}-${end + 1}"
            type == SPACED && interval == 2 -> context.getString(
                // Index count from 0
                if (start.isEven()) {
                    R.string.odd_week_description
                } else {
                    R.string.even_week_description
                }, "${start + 1}-${end + 1}"
            )
            else -> StringBuilder().apply {
                val lastIndex = timePeriodArray.lastIndex
                for ((i, period) in timePeriodArray.withIndex()) {
                    if (period.length == 1) {
                        append(period.start + 1)
                    } else {
                        append("${period.start + 1}-${period.end + 1}")
                    }
                    if (i != lastIndex) append(" ,")
                }
            }.toString()
        }
}