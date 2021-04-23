package tool.xfy9326.schedule.json.parser.pure

import kotlinx.serialization.Serializable

@Serializable
data class CourseTimeImportJSON(
    val weekNum: List<Int> = emptyList(),
    // Mon -> 1 ~ Sun -> 7
    val weekDay: Int,
    // From 1
    val start: Int,
    // From 1
    val duration: Int,
    val location: String? = null,
) {
    init {
        require(weekDay in 1..7) { "Week day empty!" }
        require(start > 0) { "Start error!" }
        require(duration > 0) { "Duration error!" }
    }
}