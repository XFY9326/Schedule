package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.make
import tool.xfy9326.schedule.content.utils.arrangeWeekNum

class CourseParseResult private constructor(val courses: List<Course>, val ignorableError: CourseAdapterException?) {

    class Builder(exceptCourseAmount: Int? = null) {
        private val courses: ArrayList<Course> =
            if (exceptCourseAmount == null) {
                ArrayList()
            } else {
                ArrayList(exceptCourseAmount)
            }
        private var error: CourseAdapterException? = null

        fun add(course: Course, combineSameCourse: Boolean = false) {
            if (combineSameCourse) {
                val found = courses.find { existCourse ->
                    course.name == existCourse.name && course.teacher == existCourse.teacher
                }
                found?.arrangeWeekNum()
                if (found == null) {
                    courses.add(course)
                } else {
                    val times = found.times.toMutableSet()
                    times.addAll(course.times)
                    found.times = times.toList()
                }
            } else {
                courses.add(course)
            }
        }

        fun withCatcher(skipError: Boolean = true, action: () -> Unit) {
            try {
                action()
            } catch (e: CourseAdapterException) {
                throw e
            } catch (e: Exception) {
                if (skipError) {
                    error = CourseAdapterException.Error.FAILED_TO_IMPORT_SOME_COURSE.make(e)
                } else {
                    throw e
                }
            }
        }

        fun add(skipErrorCourse: Boolean = true, combineSameCourse: Boolean = false, action: () -> Course?) {
            withCatcher(skipErrorCourse) {
                action()?.let { add(it, combineSameCourse) }
            }
        }

        fun build() = CourseParseResult(courses, error)
    }
}