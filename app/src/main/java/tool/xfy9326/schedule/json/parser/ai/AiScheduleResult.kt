@file:Suppress("SpellCheckingInspection")

package tool.xfy9326.schedule.json.parser.ai

import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.json.serializer.DateFormatStringSerializer
import java.util.Date

@Serializable
data class AiScheduleResult(
    val courseInfos: List<CourseInfos>,
    val sectionTimes: List<SectionTimes>,
    val pureSchedule: PureScheduleConfig? = null
) {
    @Serializable
    data class PureScheduleConfig(
        // yyyy-MM-dd
        @Serializable(DateFormatStringSerializer::class)
        val termStart: Date? = null,
        // yyyy-MM-dd
        @Serializable(DateFormatStringSerializer::class)
        val termEnd: Date? = null
    )
}