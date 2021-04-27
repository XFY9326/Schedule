package tool.xfy9326.schedule.beans

import android.content.Context
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.WeekDay.Companion.orderedValue
import tool.xfy9326.schedule.beans.WeekNumPattern.PatternType.*
import tool.xfy9326.schedule.kt.isEven
import tool.xfy9326.schedule.utils.CalendarUtils

class WeekNumPattern(weekNum: BooleanArray) {

    constructor(courseTime: CourseTime, scheduleCalculateTimes: ScheduleCalculateTimes) :
            this(fixWeekNumBySchedule(courseTime, scheduleCalculateTimes))

    enum class PatternType {
        /**
         * Empty week number array
         * Useless properties: start, end, interval
         * amount = 0
         * timePeriodArray empty
         *
         * @constructor Create EMPTY
         */
        EMPTY,

        /**
         * Single week number
         * Useless properties: interval
         * start == end
         * amount = 1
         * timePeriodArray.size = 1
         *
         * @constructor Create SINGLE
         */
        SINGLE,

        /**
         * Serial week number array
         * interval = 1
         * timePeriodArray.size = 1
         *
         * @constructor Create SERIAL
         */
        SERIAL,

        /**
         * Spaced week number array
         * timePeriodArray.size = amount
         *
         * @constructor Create SPACED
         */
        SPACED,

        /**
         * Messy week number array
         * Useless properties: start, end, interval, amount
         *
         * @constructor Create MESSY
         */
        MESSY;
    }

    data class TimePeriod(val start: Int, val end: Int) {
        constructor(start: Int) : this(start, start)

        val length = end - start + 1
    }

    companion object {
        private fun fixWeekNumBySchedule(courseTime: CourseTime, scheduleCalculateTimes: ScheduleCalculateTimes): BooleanArray {
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
            return tempWeekNum
        }

        private fun parseTimePeriodArray(weekNum: BooleanArray): Array<TimePeriod> {
            val result = ArrayList<TimePeriod>()
            var i = 0
            while (i < weekNum.size) {
                if (weekNum[i]) {
                    var j = i + 1
                    while (j < weekNum.size && weekNum[j]) {
                        j++
                    }
                    j--
                    result.add(TimePeriod(i, j))
                    i += j - i
                }
                i++
            }
            return result.toTypedArray()
        }

        private fun parseSpacedTimePeriodArray(start: Int, end: Int, interval: Int): Array<TimePeriod> {
            val result = ArrayList<TimePeriod>(((end - start) / (interval + 1)) + 1)
            for (i in start..end step interval) {
                result.add(TimePeriod(i))
            }
            return result.toTypedArray()
        }
    }

    val type: PatternType
    val start: Int
    val end: Int
    val interval: Int
    val amount: Int
    val timePeriodArray: Array<TimePeriod>

    init {
        var startIndex = 0
        var endIndex = 0
        var indexInterval = 0

        var metFirst = false
        var setInterval = false
        var messy = false

        for ((i, b) in weekNum.withIndex()) {
            if (b) {
                if (metFirst) {
                    if (setInterval) {
                        if (i - endIndex != indexInterval) {
                            messy = true
                            break
                        }
                    } else {
                        setInterval = true
                        indexInterval = i - startIndex
                    }
                } else {
                    metFirst = true
                    startIndex = i
                }
                endIndex = i
            }
        }

        type = when {
            messy -> {
                start = -1
                end = -1
                interval = -1
                amount = -1
                timePeriodArray = parseTimePeriodArray(weekNum)
                MESSY
            }
            !metFirst -> {
                start = -1
                end = -1
                interval = -1
                amount = 0
                timePeriodArray = emptyArray()
                EMPTY
            }
            startIndex == endIndex && !setInterval -> {
                start = startIndex
                end = startIndex
                interval = -1
                amount = 1
                timePeriodArray = arrayOf(TimePeriod(start))
                SINGLE
            }
            indexInterval == 1 -> {
                start = startIndex
                end = endIndex
                interval = 1
                amount = endIndex - startIndex + 1
                timePeriodArray = arrayOf(TimePeriod(start, end))
                SERIAL
            }
            else -> {
                start = startIndex
                end = endIndex
                interval = indexInterval
                amount = ((endIndex - startIndex) / (indexInterval + 1)) + 1
                timePeriodArray = parseSpacedTimePeriodArray(start, end, interval)
                SPACED
            }
        }
    }

    fun getText(context: Context) =
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