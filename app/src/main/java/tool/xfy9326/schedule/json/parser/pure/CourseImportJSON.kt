package tool.xfy9326.schedule.json.parser.pure

import kotlinx.serialization.Serializable

@Serializable
data class CourseImportJSON(
    val name: String,
    val teacher: String? = null,
    val times: List<CourseTimeImportJSON>,
) {
    init {
        require(name.isNotEmpty()) { "Course name empty!" }
    }
}