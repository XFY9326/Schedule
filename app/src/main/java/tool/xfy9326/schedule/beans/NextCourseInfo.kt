package tool.xfy9326.schedule.beans

import android.content.Context
import android.os.Parcelable
import io.github.xfy9326.atools.core.nullIfBlank
import kotlinx.parcelize.Parcelize
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.startTimeStr

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
        if (courseTeacher == null && courseLocation == null) {
            null
        } else if (courseTeacher == null) {
            courseLocation
        } else if (courseLocation == null) {
            courseTeacher
        } else {
            context.getString(R.string.next_course_description, courseTeacher, courseLocation)
        }.nullIfBlank()
}
