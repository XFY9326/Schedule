@file:Suppress("SpellCheckingInspection")

package tool.xfy9326.schedule.json.parser.ai

import kotlinx.serialization.Serializable

@Serializable
data class AiScheduleResult(
    val courseInfos: List<CourseInfos>,
    val sectionTimes: List<SectionTimes>,
)