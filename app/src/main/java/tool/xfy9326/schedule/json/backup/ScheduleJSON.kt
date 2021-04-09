package tool.xfy9326.schedule.json.backup

import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.beans.WeekDay

@Serializable
data class ScheduleJSON(
    val name: String,
    val times: List<ScheduleTimeJSON>,
    val color: Int,
    val weekStart: String,
    val startDate: Long = 0,
    val endDate: Long = 0,
    val courses: List<CourseJSON>,
) {
    init {
        require(name.isNotEmpty()) { "Schedule name empty!" }
        require(weekStart == WeekDay.MONDAY.shortName || weekStart == WeekDay.SUNDAY.shortName) { "Week start error!" }
    }
}