package tool.xfy9326.schedule.beans

import android.content.Context
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.startTimeStr
import tool.xfy9326.schedule.tools.base.nullIfBlank

@Parcelize
data class NextCourseInfo(
    val scheduleId: Long,
    val courseId: Long,
    val timeId: Long,
    val courseName: String,
    val courseColor: Int,
    val courseTeacher: String?,
    val courseLocation: String?,
    val startTime: String,
) : Parcelable {

    constructor(schedule: Schedule, course: Course, courseTime: CourseTime) : this(
        schedule.scheduleId,
        course.courseId,
        courseTime.timeId,
        course.name,
        course.color,
        course.teacher,
        courseTime.location,
        schedule.times[courseTime.sectionTime.start - 1].startTimeStr
    )

    fun getSingleLineCourseTimeDescription(context: Context) =
        when {
            courseTeacher == null && courseLocation == null -> {
                null
            }

            courseTeacher == null -> {
                courseLocation
            }

            courseLocation == null -> {
                courseTeacher
            }

            else -> {
                context.getString(R.string.next_course_description, courseTeacher, courseLocation)
            }
        }.nullIfBlank()
}
