package tool.xfy9326.schedule.json.beans

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleJSON(
    val name: String,
    val times: List<ScheduleTimeJSON>,
    val color: Int,
    val courses: List<CourseJSON>,
)