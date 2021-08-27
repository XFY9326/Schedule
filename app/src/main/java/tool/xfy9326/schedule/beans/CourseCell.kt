package tool.xfy9326.schedule.beans

import androidx.annotation.ColorInt

data class CourseCell(
    val courseId: Long,
    val timeId: Long,
    val courseName: String,
    val courseLocation: String?,
    val sectionTime: SectionTime,
    @ColorInt
    val cellColor: Int,
    val isThisWeekCourse: Boolean,
) {
    constructor(course: Course, courseTime: CourseTime, notThisWeekCourse: Boolean) :
            this(course.courseId, courseTime.timeId, course.name, courseTime.location, courseTime.sectionTime, course.color, notThisWeekCourse)
}
