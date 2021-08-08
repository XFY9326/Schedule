package tool.xfy9326.schedule.beans

import android.content.Context
import kotlinx.serialization.Serializable
import lib.xfy9326.kit.nullIfBlank
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.startTimeStr

@Serializable
data class NextCourseInfo(
    val courseId: Long,
    val timeId: Long,
    val name: String,
    val color: Int,
    val description: String?,
    val startTime: String,
) {
    companion object {
        private fun getCourseDescription(context: Context, course: Course, courseTime: CourseTime) =
            if (course.teacher == null && courseTime.location == null) {
                null
            } else if (course.teacher == null) {
                courseTime.location
            } else if (courseTime.location == null) {
                course.teacher
            } else {
                context.getString(R.string.next_course_description, course.teacher, courseTime.location)
            }.nullIfBlank()
    }

    constructor(context: Context, schedule: Schedule, course: Course, courseTime: CourseTime) : this(
        course.courseId,
        courseTime.timeId,
        course.name,
        course.color,
        getCourseDescription(context, course, courseTime),
        schedule.times[courseTime.classTime.classStartTime - 1].startTimeStr
    )
}
