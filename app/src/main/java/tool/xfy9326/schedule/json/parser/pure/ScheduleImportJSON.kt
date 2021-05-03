package tool.xfy9326.schedule.json.parser.pure

import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.json.ScheduleTimeJSON

@Serializable
data class ScheduleImportJSON(
    val version: Int = VERSION,
    val times: List<ScheduleTimeJSON>,
    val courses: List<CourseImportJSON>,
    // yyyy-MM-dd
    val termStart: String? = null,
    // yyyy-MM-dd
    val termEnd: String? = null,
) {
    companion object {
        private const val VERSION = 1
    }
}