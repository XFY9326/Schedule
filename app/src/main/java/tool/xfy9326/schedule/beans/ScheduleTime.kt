package tool.xfy9326.schedule.beans

import androidx.annotation.IntRange
import tool.xfy9326.schedule.utils.CalendarUtils
import java.io.Serializable
import java.util.*

data class ScheduleTime(
    @IntRange(from = 0, to = 23)
    var startHour: Int,
    @IntRange(from = 0, to = 59)
    var startMinute: Int,
    @IntRange(from = 0, to = 23)
    var endHour: Int,
    @IntRange(from = 0, to = 59)
    var endMinute: Int,
) : Serializable {
    companion object {
        private const val HOUR_MINUTE_DIVIDE = ":"
        private const val TIME_DIVIDE = "~"

        fun serialize(arr: List<ScheduleTime>) =
            buildString {
                for (time in arr) {
                    append("%02d%02d%02d%02d".format(time.startHour, time.startMinute, time.endHour, time.endMinute))
                }
            }

        fun deserialize(str: String) = Array(str.length / 8) {
            val base = it * 8
            ScheduleTime(
                str.substring(base, base + 2).toInt(),
                str.substring(base + 2, base + 4).toInt(),
                str.substring(base + 4, base + 6).toInt(),
                str.substring(base + 6, base + 8).toInt()
            )
        }.toList()

        private fun timeAdd(hour: Int, minute: Int, addMinute: Int): Pair<Int, Int> {
            CalendarUtils.getCalendar().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                add(Calendar.MINUTE, addMinute)
                return get(Calendar.HOUR_OF_DAY) to get(Calendar.MINUTE)
            }
        }

        fun fromTimeStr(startTimeStr: String, endTimeStr: String): ScheduleTime {
            val start = parseTimeStr(startTimeStr)
            val end = parseTimeStr(endTimeStr)
            return ScheduleTime(start.first, start.second, end.first, end.second)
        }

        private fun parseTimeStr(str: String): Pair<Int, Int> {
            require(HOUR_MINUTE_DIVIDE in str) { "Time str format error!" }
            val num = str.split(HOUR_MINUTE_DIVIDE)
            return num[0].toInt() to num[1].toInt()
        }
    }

    fun setDuration(minute: Int) {
        timeAdd(startHour, startMinute, minute).let {
            endHour = it.first
            endMinute = it.second
        }
    }

    fun endTimeMove(minute: Int) = timeAdd(endHour, endMinute, minute)

    fun createNew(breakCostMinute: Int, courseCostMinute: Int): ScheduleTime {
        val start = timeAdd(endHour, endMinute, breakCostMinute)
        val end = timeAdd(endHour, endMinute, breakCostMinute + courseCostMinute)
        return ScheduleTime(start.first, start.second, end.first, end.second)
    }

    val startTimeStr
        get() = "%02d$HOUR_MINUTE_DIVIDE%02d".format(startHour, startMinute)
    val endTimeStr
        get() = "%02d$HOUR_MINUTE_DIVIDE%02d".format(endHour, endMinute)

    override fun toString(): String = "$startTimeStr$TIME_DIVIDE$endTimeStr"
}