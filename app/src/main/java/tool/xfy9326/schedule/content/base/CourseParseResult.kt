package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.make

class CourseParseResult private constructor(val courses: List<Course>, val ignorableError: CourseAdapterException?) {

    class Builder(exceptCourseAmount: Int? = null) {
        private val courses: ArrayList<Course> =
            if (exceptCourseAmount == null) {
                ArrayList()
            } else {
                ArrayList(exceptCourseAmount)
            }
        private var error: CourseAdapterException? = null

        fun add(skipErrorCourse: Boolean = true, combineSameCourse: Boolean = false, action: () -> Course?) {
            try {
                action()?.let {
                    if (combineSameCourse) {
                        val found = courses.find { existCourse ->
                            it.name == existCourse.name && it.teacher == existCourse.teacher
                        }
                        if (found == null) {
                            courses.add(it)
                        } else {
                            val times = found.times.toMutableSet()
                            times.addAll(it.times)
                            found.times = times.toList()
                        }
                    } else {
                        courses.add(it)
                    }
                }
            } catch (e: CourseAdapterException) {
                throw e
            } catch (e: Exception) {
                if (skipErrorCourse) {
                    error = CourseAdapterException.Error.FAILED_TO_IMPORT_SOME_COURSE.make(e)
                } else {
                    throw e
                }
            }
        }

        fun build() = CourseParseResult(courses, error)
    }
}