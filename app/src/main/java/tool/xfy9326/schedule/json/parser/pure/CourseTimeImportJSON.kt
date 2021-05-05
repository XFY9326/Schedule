package tool.xfy9326.schedule.json.parser.pure

import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.json.serializer.WeekDayIntSerializer

@Serializable
data class CourseTimeImportJSON(
    val weekNum: List<Int> = emptyList(),
    @Serializable(WeekDayIntSerializer::class)
    val weekDay: WeekDay,
    // From 1
    val start: Int,
    // From 1
    val duration: Int,
    val location: String? = null,
) {
    init {
        require(start > 0) { "Start error!" }
        require(duration > 0) { "Duration error!" }
    }
}