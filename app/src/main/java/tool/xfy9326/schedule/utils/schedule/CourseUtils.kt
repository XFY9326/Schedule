package tool.xfy9326.schedule.utils.schedule

import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.arrangeWeekNum
import tool.xfy9326.schedule.content.utils.hasCourse
import tool.xfy9326.schedule.io.IOManager
import tool.xfy9326.schedule.kt.forEachTwo
import tool.xfy9326.schedule.kt.intersect
import tool.xfy9326.schedule.kt.iterateAll
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.utils.CalendarUtils
import kotlin.math.max

object CourseUtils {
    fun getScheduleViewDataByWeek(weekNum: Int, bundle: ScheduleBuildBundle): ScheduleViewData {
        val result = ArrayList<CourseCell>()
        val showNotThisWeekCourse = bundle.scheduleStyles.showNotThisWeekCourse
        val maxWeekNum = CourseTimeUtils.getMaxWeekNum(bundle.schedule.startDate, bundle.schedule.endDate, bundle.schedule.weekStart)
        val startWeekDay = CalendarUtils.getWeekDay(bundle.schedule.startDate)
        val endWeekDay = CalendarUtils.getWeekDay(bundle.schedule.endDate)

        bundle.courses.iterateAll { course, courseTime ->
            val isThisWeekCourse =
                hasThisWeekCourseBySchedule(courseTime, weekNum, maxWeekNum, startWeekDay, endWeekDay, bundle.schedule.weekStart)
            if (isThisWeekCourse || showNotThisWeekCourse) {
                val intersectCells = result.filter {
                    it.classTime intersect courseTime.classTime
                }

                if (intersectCells.isEmpty()) {
                    result.add(CourseCell(course, courseTime, isThisWeekCourse))
                } else {
                    intersectCells.toMutableList().apply {
                        add(CourseCell(course, courseTime, isThisWeekCourse))
                        sortWith { a, b ->
                            if (a.isThisWeekCourse && !b.isThisWeekCourse) {
                                -1
                            } else if (b.isThisWeekCourse && !a.isThisWeekCourse) {
                                1
                            } else {
                                a.classTime.compareTo(b.classTime)
                            }
                        }
                        result.removeAll(this)
                        result.add(first())
                    }
                }
            }
        }

        return ScheduleViewData(weekNum, bundle.schedule, result, bundle.scheduleStyles)
    }

    private fun hasThisWeekCourseBySchedule(
        courseTime: CourseTime,
        weekNum: Int,
        maxWeekNum: Int,
        startWeekDay: WeekDay,
        endWeekDay: WeekDay,
        weekStart: WeekDay,
    ) =
        if (courseTime.weekNum.hasCourse(weekNum)) {
            when (weekNum) {
                1 -> startWeekDay.orderedValue(weekStart) <= courseTime.classTime.weekDay.orderedValue(weekStart)
                maxWeekNum -> endWeekDay.orderedValue(weekStart) >= courseTime.classTime.weekDay.orderedValue(weekStart)
                else -> true
            }
        } else {
            false
        }

    fun getMaxWeekNum(courses: List<Course>): Int {
        var defaultValue = 1
        courses.iterateAll { _, courseTime ->
            courseTime.weekNum = courseTime.weekNum.arrangeWeekNum()
            defaultValue = max(defaultValue, courseTime.weekNum.size)
        }
        return defaultValue
    }

    fun hasWeekendCourse(cells: List<CourseCell>): Boolean {
        for (cell in cells) {
            if (cell.classTime.weekDay.isWeekend) {
                return true
            }
        }
        return false
    }

    fun solveConflicts(scheduleTimes: List<ScheduleTime>, courses: List<Course>): Boolean {
        val allTimes = courses.flatMap {
            it.times
        }

        var foundConflicts = false

        allTimes.forEach {
            if (it.classTime.classEndTime > scheduleTimes.size) CourseAdapterException.Error.MAX_COURSE_NUM_ERROR.report()
        }

        allTimes.forEachTwo { _, t1, _, t2 ->
            if (t1 intersect t2) {
                foundConflicts = true
                t2.weekNum = BooleanArray(0)
            }
        }

        return foundConflicts
    }

    fun createNewCourse(scheduleId: Long) =
        Course(0, scheduleId, IOManager.resources.getString(R.string.new_course), null, MaterialColorHelper.random(), listOf(createNewCourseTime()))

    fun createNewCourseTime(maxWeekNum: Int = 0) =
        CourseTime(if (maxWeekNum <= 0) {
            BooleanArray(0)
        } else {
            BooleanArray(maxWeekNum) { false }
        }, WeekDay.MONDAY, 1, 1, null)

    fun validateCourse(course: Course, otherCourses: List<Course>): EditError? {
        if (course.name.isBlank() || course.name.isEmpty()) {
            return EditError.Type.COURSE_NAME_EMPTY.make()
        }

        if (course.times.isEmpty()) {
            EditError.Type.COURSE_TIME_LIST_EMPTY.make()
        }

        course.times.forEachTwo { i1, courseTime1, i2, courseTime2 ->
            if (courseTime1 intersect courseTime2) return EditError.Type.COURSE_TIME_INNER_CONFLICT_ERROR.make(i1 + 1, i2 + 1)
        }

        for (others in otherCourses) {
            for (time in others.times) {
                for ((i, courseTime) in course.times.withIndex()) {
                    if (courseTime intersect time) {
                        return EditError.Type.COURSE_TIME_OTHERS_CONFLICT_ERROR.make(i + 1, others.name)
                    }
                }
            }
        }

        return null
    }
}