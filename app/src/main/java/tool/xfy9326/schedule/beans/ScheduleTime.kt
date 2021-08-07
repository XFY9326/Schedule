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

        val ScheduleTime.startTimeStr
            get() = "%02d$HOUR_MINUTE_DIVIDE%02d".format(startHour, startMinute)
        val ScheduleTime.endTimeStr
            get() = "%02d$HOUR_MINUTE_DIVIDE%02d".format(endHour, endMinute)

        fun ScheduleTime.compare(hour: Int, minute: Int): Int {
            val m = this.getFixedMinutesInDay()
            val input = hour * 60 + minute
            return when {
                input < m.first -> -1
                input > m.second -> 1
                else -> 0
            }
        }

        private fun ScheduleTime.getFixedMinutesInDay(): Pair<Int, Int> {
            var start = this.startHour * 60 + this.startMinute
            val end = this.endHour * 60 + this.endMinute
            if (start > end) {
                start -= 24 * 60
            }
            return start to end
        }

        infix fun ScheduleTime.intersect(scheduleTime: ScheduleTime): Boolean {
            val m1 = this.getFixedMinutesInDay()
            val m2 = scheduleTime.getFixedMinutesInDay()
            return m1.first <= m2.second && m1.second >= m2.first
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

    override fun toString(): String = "$startTimeStr$TIME_DIVIDE$endTimeStr"
}