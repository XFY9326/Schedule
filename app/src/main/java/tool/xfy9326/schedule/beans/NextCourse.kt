package tool.xfy9326.schedule.beans

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NextCourse(
    val isVacation: Boolean,
    val noNextCourse: Boolean,
    val nextCourseInfo: NextCourseInfo?,
    val nextAutoRefreshTimeMills: Long,
) : Parcelable