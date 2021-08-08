package tool.xfy9326.schedule.beans

import kotlinx.serialization.Serializable

@Serializable
data class NextCourse(
    val isVacation: Boolean,
    val noNextCourse: Boolean,
    val nextCourseInfo: NextCourseInfo?,
    val nextAutoRefreshTime: Long,
)