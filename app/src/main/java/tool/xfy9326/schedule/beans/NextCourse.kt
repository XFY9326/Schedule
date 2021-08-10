package tool.xfy9326.schedule.beans

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class NextCourse(
    val isVacation: Boolean,
    val noNextCourse: Boolean,
    val nextCourseInfo: NextCourseInfo?,
    val nextAutoRefreshTime: Long,
) : Parcelable