package tool.xfy9326.schedule.json.backup

import androidx.annotation.ColorInt
import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.json.ScheduleTimeJSON
import tool.xfy9326.schedule.json.serializer.DateLongSerializer
import tool.xfy9326.schedule.json.serializer.WeekDayShortNameSerializer
import java.util.*

@Serializable
data class ScheduleJSON(
    val name: String,
    val times: List<ScheduleTimeJSON>,
    @ColorInt
    val color: Int,
    @Serializable(WeekDayShortNameSerializer::class)
    val weekStart: WeekDay,
    @Serializable(DateLongSerializer::class)
    val startDate: Date? = null,
    @Serializable(DateLongSerializer::class)
    val endDate: Date? = null,
    val courses: List<CourseJSON>,
) {
    init {
        require(name.isNotEmpty()) { "Schedule name empty!" }
        require(weekStart == WeekDay.MONDAY || weekStart == WeekDay.SUNDAY) { "Week start error!" }
    }
}