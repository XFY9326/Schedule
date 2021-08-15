@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.base

import androidx.collection.*
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.Course.Companion.arrangeWeekNum
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.make
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils

class CourseParseResult private constructor(val courses: List<Course>, val ignorableError: CourseAdapterException?) {
    companion object {
        val EMPTY = CourseParseResult(emptyList(), null)
    }

    class Builder(exceptCourseAmount: Int? = null) {
        private val courses: ArrayList<Course> = if (exceptCourseAmount == null) ArrayList() else ArrayList(exceptCourseAmount)
        private var error: CourseAdapterException? = null

        private fun Course.combinableHashCode() =
            name.hashCode() + 31 * (teacher?.hashCode() ?: 0)

        private fun CourseTime.combinableHashCode() =
            weekNum.contentHashCode() + 31 * classTime.weekDay.hashCode() + 31 * (location?.hashCode() ?: 0)

        fun add(course: Course) {
            course.arrangeWeekNum()
            courses.add(course)
        }

        fun withCatcher(skipError: Boolean = true, action: () -> Unit): Boolean {
            try {
                action()
                return true
            } catch (e: CourseAdapterException) {
                throw e
            } catch (e: Exception) {
                if (skipError) {
                    error = CourseAdapterException.Error.FAILED_TO_IMPORT_SOME_COURSE.make(e)
                } else {
                    throw e
                }
            }
            return false
        }

        fun add(skipErrorCourse: Boolean = true, action: () -> Course?) {
            withCatcher(skipErrorCourse) {
                action()?.let { add(it) }
            }
        }

        private fun timePeriodToHashSet(start: Int, duration: Int): HashSet<Int> {
            val result = HashSet<Int>()
            for (i in start until start + duration) {
                result.add(i)
            }
            return result
        }

        private fun timePeriodAddToHashSet(start: Int, duration: Int, set: HashSet<Int>) {
            for (i in start until start + duration) {
                set.add(i)
            }
        }

        private fun combinedCourseTime(courseTimeMap: SparseArrayCompat<Pair<CourseTime, HashSet<Int>>>, courseTimes: List<CourseTime>): List<CourseTime> {
            if (courseTimes.size < 2) return courseTimes

            courseTimeMap.clear()
            for (courseTime in courseTimes) {
                val hashCode = courseTime.combinableHashCode()
                if (hashCode in courseTimeMap) {
                    courseTimeMap[hashCode]?.let {
                        timePeriodAddToHashSet(courseTime.classTime.classStartTime, courseTime.classTime.classDuration, it.second)
                    }
                } else {
                    courseTimeMap[hashCode] = courseTime to timePeriodToHashSet(courseTime.classTime.classStartTime, courseTime.classTime.classDuration)
                }
            }
            val result = ArrayList<CourseTime>(courseTimeMap.size)
            for (pair in courseTimeMap.valueIterator()) {
                val timePeriods = CourseAdapterUtils.parseIntCollectionPeriod(pair.second)
                for (timePeriod in timePeriods) {
                    result.add(pair.first.copy(classTime = pair.first.classTime.copy(classStartTime = timePeriod.start, classDuration = timePeriod.length)))
                }
            }
            return result
        }

        private fun combineCourse(courses: List<Course>): List<Course> {
            if (courses.size < 2) return courses

            val courseMap = SparseArrayCompat<Pair<Course, LinkedHashSet<CourseTime>>>()

            for (course in courses) {
                val hashCode = course.combinableHashCode()
                if (hashCode in courseMap) {
                    courseMap[hashCode]?.second?.addAll(course.times)
                } else {
                    courseMap[hashCode] = course to LinkedHashSet(course.times)
                }
            }
            return courseMap.valueIterator().asSequence().map {
                it.first.apply {
                    times = it.second.toList()
                }
            }.toList()
        }

        fun build(combineCourse: Boolean = false, combineCourseTime: Boolean = false): CourseParseResult {
            var resultList: List<Course> = courses

            if (combineCourse) {
                resultList = combineCourse(resultList)
            }

            if (combineCourseTime) {
                val courseTimeMap = SparseArrayCompat<Pair<CourseTime, HashSet<Int>>>()
                for (course in resultList) {
                    course.times = combinedCourseTime(courseTimeMap, course.times)
                }
            }

            return CourseParseResult(resultList, error)
        }
    }
}