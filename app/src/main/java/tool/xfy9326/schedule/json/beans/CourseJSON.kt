package tool.xfy9326.schedule.json.beans

import kotlinx.serialization.Serializable

@Serializable
data class CourseJSON(
    val name: String,
    val teacher: String? = null,
    val color: Int,
    val times: List<CourseTimeJSON>,
)