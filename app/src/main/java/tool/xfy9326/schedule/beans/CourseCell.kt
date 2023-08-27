package tool.xfy9326.schedule.beans

import androidx.annotation.ColorInt

data class CourseCell(
    val courseId: Long,
    val timeId: Long,
    val courseName: String,
    val courseLocation: String?,
    val courseTeacher: String?,
    val sectionTime: SectionTime,
    @ColorInt
    val cellColor: Int,
    // This course cell data may comes from different weeks if it's not this week's course.
    // If and only if isThisWeekCourse is true, fromWeekNum equals to this week's week number.
    val fromWeekNum: Int,
    val isThisWeekCourse: Boolean,
) {

    constructor(course: Course, courseTime: CourseTime, weekNum: Int, isThisWeekCourse: Boolean) :
            this(
                course.courseId, courseTime.timeId, course.name, courseTime.location, course.teacher,
                courseTime.sectionTime, course.color, weekNum, isThisWeekCourse
            )

    operator fun compareTo(courseCell: CourseCell): Int {
        if (this === courseCell) return 0
        return if (fromWeekNum == courseCell.fromWeekNum) {
            sectionTime.compareTo(courseCell.sectionTime)
        } else {
            fromWeekNum.compareTo(courseCell.fromWeekNum)
        }
    }
}
