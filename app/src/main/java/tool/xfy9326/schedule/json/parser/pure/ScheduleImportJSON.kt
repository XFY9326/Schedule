package tool.xfy9326.schedule.json.parser.pure

import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.json.ScheduleTimeJSON
import tool.xfy9326.schedule.json.serializer.DateFormatStringSerializer
import java.util.*

@Serializable
data class ScheduleImportJSON(
    val version: Int = VERSION,
    val times: List<ScheduleTimeJSON>,
    val courses: List<CourseImportJSON>,
    // yyyy-MM-dd
    @Serializable(DateFormatStringSerializer::class)
    val termStart: Date? = null,
    // yyyy-MM-dd
    @Serializable(DateFormatStringSerializer::class)
    val termEnd: Date? = null,
) {
    companion object {
        private const val VERSION = 1
    }
}