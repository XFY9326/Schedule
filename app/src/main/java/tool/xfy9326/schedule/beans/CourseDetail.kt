package tool.xfy9326.schedule.beans

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourseDetail(
    val currentTimeId: Long,
    val course: Course,
    val scheduleTimes: Schedule.Times,
) : Parcelable
