package tool.xfy9326.schedule.beans

import androidx.annotation.IntRange
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.*

data class Day(
    @IntRange(from = 1, to = 12)
    val month: Int,
    @IntRange(from = 1, to = 31)
    val day: Int,
    val weekDay: WeekDay,
) {
    fun isToday(): Boolean {
        CalendarUtils.getCalendar().apply {
            return month == get(Calendar.MONTH) + 1 && day == get(Calendar.DATE)
        }
    }
}