package tool.xfy9326.schedule.json.parser.pure

import androidx.annotation.IntRange
import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.json.serializer.WeekDayIntSerializer

@Serializable
data class CourseTimeImportJSON(
    // From 1
    val weekNum: List<Int> = emptyList(),
    @Serializable(WeekDayIntSerializer::class)
    val weekDay: WeekDay,
    @IntRange(from = 1)
    val start: Int? = null,
    @IntRange(from = 1)
    val duration: Int? = null,
    // From 1
    val sections: List<Int>? = null,
    val location: String? = null,
) {
    init {
        if (start != null && duration != null) {
            require(start > 0) { "Start error!" }
            require(duration > 0) { "Duration error!" }
        } else {
            require(sections != null) { "Sections or start and duration error!" }
        }
    }
}