package tool.xfy9326.schedule.json.parser.ai

import kotlinx.serialization.Serializable

@Serializable
data class SectionTimes(
    val section: Int,
    val startTime: String,
    val endTime: String,
)