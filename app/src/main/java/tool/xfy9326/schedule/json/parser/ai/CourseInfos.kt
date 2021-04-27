@file:Suppress("SpellCheckingInspection")

package tool.xfy9326.schedule.json.parser.ai

import kotlinx.serialization.Serializable

@Serializable
data class CourseInfos(
    val name: String,
    val position: String?,
    val teacher: String?,
    val weeks: List<Int>,
    val day: Int,
    val sections: List<Section>,
) {
    @Serializable
    data class Section(
        val section: Int,
    )
}