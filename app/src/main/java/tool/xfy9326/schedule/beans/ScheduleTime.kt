package tool.xfy9326.schedule.beans

import android.os.Parcelable
import androidx.annotation.IntRange
import kotlinx.parcelize.Parcelize
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class ScheduleTime(
    @IntRange(from = 0, to = 23)
    var startHour: Int,
    @IntRange(from = 0, to = 59)
    var startMinute: Int,
    @IntRange(from = 0, to = 23)
    var endHour: Int,
    @IntRange(from = 0, to = 59)
    var endMinute: Int,
) : Parcelable {
    companion object {
        private const val HOUR_MINUTE_DIVIDE = ":"
        private const val TIME_DIVIDE = "~"

        infix fun ScheduleTime.intersect(scheduleTime: ScheduleTime): Boolean {
            var start1 = this.startHour * 60 + this.startMinute
            val end1 = this.endHour * 60 + this.endMinute
            if (start1 > end1) {
                start1 -= 24 * 60
            }

            var start2 = scheduleTime.startHour * 60 + scheduleTime.startMinute
            val end2 = scheduleTime.endHour * 60 + scheduleTime.endMinute
            if (start2 > end2) {
                start2 -= 24 * 60
            }

            return start1 <= end2 && end1 >= start2
        }

        fun listOf(vararg numArr: Int): List<ScheduleTime> {
            require(numArr.size % 4 == 0)

            val result = ArrayList<ScheduleTime>(numArr.size / 4)
            for (i in numArr.indices step 4) {
                result.add(ScheduleTime(numArr[i], numArr[i + 1], numArr[i + 2], numArr[i + 3]))
            }
            return result
        }

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