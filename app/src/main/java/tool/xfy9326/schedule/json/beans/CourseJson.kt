package tool.xfy9326.schedule.json.beans

import kotlinx.serialization.Serializable

@Serializable
data class CourseJson(
    val name: String,
    val teacher: String? = null,
    val color: Int,
    val times: List<CourseTimeJson>,
    val _version: Int = VERSION,
) {
    companion object {
        private const val VERSION = 1
    }
}
