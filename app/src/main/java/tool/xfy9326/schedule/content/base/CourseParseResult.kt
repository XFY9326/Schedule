package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.content.utils.CourseAdapterException

class CourseParseResult private constructor(val courses: List<Course>, val ignorableError: CourseAdapterException?) {

    class Builder(exceptCourseAmount: Int? = null) {
        private val courses: ArrayList<Course> =
            if (exceptCourseAmount == null) {
                ArrayList()
            } else {
                ArrayList(exceptCourseAmount)
            }
        private var error: CourseAdapterException? = null

        fun add(skipErrorCourse: Boolean = true, action: () -> Course?) {
            try {
                action()?.let(courses::add)
            } catch (e: Exception) {
                if (skipErrorCourse && e !is CourseAdapterException) {
                    error = CourseAdapterException.Error.FAILED_TO_IMPORT_SOME_COURSE.make(e)
                } else {
                    throw e
                }
            }
        }

        fun build() = CourseParseResult(courses, error)
    }
}