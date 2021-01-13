package tool.xfy9326.schedule.json.beans

import kotlinx.serialization.Serializable

@Serializable
data class CourseTimeJson(
    val weekNum: String,
    val weekDay: String,
    val start: Int,
    val duration: Int,
    val location: String? = null,
    val _version: Int = VERSION,
) {
    companion object {
        private const val VERSION = 1
    }
}
