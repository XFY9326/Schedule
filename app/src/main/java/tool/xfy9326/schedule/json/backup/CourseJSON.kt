package tool.xfy9326.schedule.json.backup

import androidx.annotation.ColorInt
import kotlinx.serialization.Serializable

@Serializable
data class CourseJSON(
    val name: String,
    val teacher: String? = null,
    @ColorInt
    val color: Int,
    val times: List<CourseTimeJSON>,
) {
    init {
        require(name.isNotEmpty()) { "Course name empty!" }
    }
}