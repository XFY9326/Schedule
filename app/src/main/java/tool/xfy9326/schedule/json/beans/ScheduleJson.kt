package tool.xfy9326.schedule.json.beans

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleJson(
    val name: String,
    val times: List<ScheduleTimeJson>,
    val color: Int,
    val courses: List<CourseJson>,
    val _version: Int = VERSION,
) {
    companion object {
        private const val VERSION = 1
    }
}
