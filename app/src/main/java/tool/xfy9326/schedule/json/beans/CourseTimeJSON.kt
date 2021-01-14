package tool.xfy9326.schedule.json.beans

import kotlinx.serialization.Serializable

@Serializable
data class CourseTimeJSON(
    val weekNum: String,
    val weekDay: String,
    val start: Int,
    val duration: Int,
    val location: String? = null,
) {
    init {
        require(weekDay.isNotEmpty()) { "Week day empty!" }
    }
}