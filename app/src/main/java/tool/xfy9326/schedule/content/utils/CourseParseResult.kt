package tool.xfy9326.schedule.content.utils

import androidx.collection.SparseArrayCompat
import androidx.collection.contains
import androidx.collection.forEach
import androidx.collection.set
import androidx.collection.size
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.Course.Companion.arrangeWeekNum
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.make
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.strictModeOnly
import tool.xfy9326.schedule.utils.NEW_LINE
import java.io.Serializable

class CourseParseResult private constructor(val courses: List<Course>, val ignorableError: CourseAdapterException?) {
    companion object {
        val EMPTY = CourseParseResult(emptyList(), null)
    }

    class Params(
        val combineCourse: Boolean = false,
        val combineCourseTime: Boolean = false,
        val combineCourseTeacher: Boolean = false,
        val combineCourseTimeLocation: Boolean = false
    ) : Serializable

    class Builder(exceptCourseAmount: Int? = null) {
        private val courses: ArrayList<Course> = if (exceptCourseAmount == null) ArrayList() else ArrayList(exceptCourseAmount)
        private var error: CourseAdapterException? = null

        private fun Course.combinableHashCode(hashTeacher: Boolean) =
            name.hashCode() + if (hashTeacher) +31 * (teacher?.hashCode() ?: 0) else 0

        private fun CourseTime.combinableHashCode(hashLocation: Boolean) =
            weekNumArray.contentHashCode() + 31 * sectionTime.weekDay.hashCode() + if (hashLocation) 31 * (location?.hashCode() ?: 0) else 0

        fun add(course: Course) {
            course.arrangeWeekNum()
            courses.add(course)
        }

        fun withCatcher(skipUnknownError: Boolean = true, action: () -> Unit): Boolean {
            try {
                action()
                return true
            } catch (e: CourseAdapterException) {
                if (e.type.strictModeOnly && skipUnknownError) {
                    error = e
                } else {
                    throw e
                }
            } catch (e: Exception) {
                if (skipUnknownError) {
                    error = CourseAdapterException.Error.FAILED_TO_IMPORT_SOME_COURSE.make(e)
                } else {
                    throw e
                }
            }
            return false
        }

        fun add(skipUnknownErrorCourse: Boolean = true, action: () -> Course?): Boolean =
            withCatcher(skipUnknownErrorCourse) {
                action()?.let { add(it) }
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

        private fun combinedCourseTime(courseTimes: List<CourseTime>, combineLocation: Boolean): List<CourseTime> {
            if (courseTimes.size < 2) return courseTimes

            val courseTimeMap = SparseArrayCompat<Pair<CourseTime, HashSet<Int>>>()
            val locationMap = SparseArrayCompat<MutableSet<String>>()

            for (courseTime in courseTimes) {
                val hashCode = courseTime.combinableHashCode(!combineLocation)
                if (hashCode in courseTimeMap) {
                    courseTimeMap[hashCode]?.let {
                        timePeriodAddToHashSet(courseTime.sectionTime.start, courseTime.sectionTime.duration, it.second)
                    }
                } else {
                    courseTimeMap[hashCode] = courseTime to timePeriodToHashSet(courseTime.sectionTime.start, courseTime.sectionTime.duration)
                }
                if (combineLocation) {
                    courseTime.location?.let {
                        if (hashCode in locationMap) {
                            locationMap[hashCode]?.add(it)
                        } else {
                            locationMap[hashCode] = mutableSetOf(it)
                        }
                    }
                }
            }

            return buildList(courseTimeMap.size) {
                courseTimeMap.forEach { hashCode, (courseTime, timePeriods) ->
                    val location = if (combineLocation) {
                        locationMap[hashCode]?.sorted()?.joinToString(NEW_LINE) ?: courseTime.location
                    } else courseTime.location
                    for (timePeriod in CourseAdapterUtils.parseIntCollectionPeriod(timePeriods)) {
                        add(
                            courseTime.copy(
                                sectionTime = courseTime.sectionTime.copy(start = timePeriod.start, duration = timePeriod.length),
                                location = location
                            )
                        )
                    }
                }
            }
        }

        private fun combineCourse(courses: List<Course>, combineTeacher: Boolean): List<Course> {
            if (courses.size < 2) return courses

            val courseMap = SparseArrayCompat<Pair<Course, LinkedHashSet<CourseTime>>>()
            val teacherMap = SparseArrayCompat<MutableSet<String>>()

            for (course in courses) {
                val hashCode = course.combinableHashCode(!combineTeacher)
                if (hashCode in courseMap) {
                    courseMap[hashCode]?.second?.addAll(course.times)
                } else {
                    courseMap[hashCode] = course to LinkedHashSet(course.times)
                }
                if (combineTeacher) {
                    course.teacher?.let {
                        if (hashCode in teacherMap) {
                            teacherMap[hashCode]?.add(it)
                        } else {
                            teacherMap[hashCode] = mutableSetOf(it)
                        }
                    }
                }
            }
            return buildList(courseMap.size) {
                courseMap.forEach { hashCode, (course, courseTimes) ->
                    if (combineTeacher) course.teacher = teacherMap[hashCode]?.sorted()?.joinToString()
                    course.times = courseTimes.toList()
                    add(course)
                }
            }
        }

        fun build(params: Params = Params()): CourseParseResult {
            var resultList: List<Course> = courses

            if (params.combineCourse) {
                resultList = combineCourse(resultList, params.combineCourseTeacher)
            }

            if (params.combineCourseTime) {
                for (course in resultList) {
                    course.times = combinedCourseTime(course.times, params.combineCourseTimeLocation)
                }
            }

            return CourseParseResult(resultList, error)
        }
    }
}