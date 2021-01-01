package tool.xfy9326.schedule.utils

import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.kt.fit
import tool.xfy9326.schedule.kt.forEachTwo
import tool.xfy9326.schedule.kt.intersect
import tool.xfy9326.schedule.kt.iterateAll
import tool.xfy9326.schedule.tools.MaterialColorHelper
import kotlin.math.max

object CourseManager {
    fun getScheduleViewDataByWeek(weekNum: Int, schedule: Schedule, courses: Array<Course>, showNotThisWeekCourse: Boolean): ScheduleViewData {
        val result = ArrayList<CourseCell>()

        courses.iterateAll { course, courseTime ->
            val isThisWeekCourse = courseTime.hasThisWeekCourse(weekNum)
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

        return ScheduleViewData(weekNum, schedule, result.toTypedArray())
    }

    fun getMaxWeekNum(courses: Array<Course>): Int {
        var defaultValue = 1
        courses.iterateAll { _, courseTime ->
            courseTime.weekNum = courseTime.weekNum.fit()
            defaultValue = max(defaultValue, courseTime.weekNum.size)
        }
        return defaultValue
    }

    fun hasWeekendCourse(cells: Array<CourseCell>): Boolean {
        for (cell in cells) {
            if (cell.classTime.weekDay.isWeekend) {
                return true
            }
        }
        return false
    }

    fun solveConflicts(scheduleTimes: Array<ScheduleTime>, courses: Array<Course>): Boolean {
        val allTimes = courses.flatMap {
            it.times
        }

        var foundConflicts = false

        allTimes.forEach {
            if (it.classTime.classEndTime > scheduleTimes.size) CourseAdapterException.ErrorType.MAX_COURSE_NUM_ERROR.report()
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
        Course(0, scheduleId, App.instance.getString(R.string.new_course), null, MaterialColorHelper.random(), listOf(createNewCourseTime()))

    fun createNewCourseTime(maxWeekNum: Int = 0) =
        CourseTime(if (maxWeekNum <= 0) {
            BooleanArray(0)
        } else {
            BooleanArray(maxWeekNum) { true }
        }, WeekDay.MONDAY, 1, 1, null)
}