package tool.xfy9326.schedule.utils.schedule

import lib.xfy9326.android.kit.io.IOManager
import lib.xfy9326.kit.forEachTwo
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.beans.Course.Companion.iterateAll
import tool.xfy9326.schedule.beans.CourseTime.Companion.getFromWeekNum
import tool.xfy9326.schedule.beans.CourseTime.Companion.hasThisWeekCourse
import tool.xfy9326.schedule.beans.CourseTime.Companion.intersect
import tool.xfy9326.schedule.beans.SectionTime.Companion.end
import tool.xfy9326.schedule.beans.SectionTime.Companion.intersect
import tool.xfy9326.schedule.beans.WeekDay.Companion.isWeekend
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.arrangeWeekNum
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.*
import kotlin.math.max

object CourseUtils {
    fun getScheduleViewDataByWeek(weekNum: Int, bundle: ScheduleBuildBundle): ScheduleViewData {
        val resultSet = TreeSet<CourseCell> { o1, o2 -> o1.compareTo(o2) }
        val showNotThisWeekCourse = bundle.scheduleStyles.showNotThisWeekCourse
        val maxWeekNum = CourseTimeUtils.getMaxWeekNum(bundle.schedule.startDate, bundle.schedule.endDate, bundle.schedule.weekStart)
        val startWeekDay = CalendarUtils.getWeekDay(bundle.schedule.startDate)
        val endWeekDay = CalendarUtils.getWeekDay(bundle.schedule.endDate)

        val replaceList = ArrayList<CourseCell>()
        bundle.courses.iterateAll { course, courseTime ->
            val isThisWeekCourse = courseTime.hasThisWeekCourse(
                weekNum = weekNum, maxWeekNum = maxWeekNum,
                startWeekDay = startWeekDay, endWeekDay = endWeekDay,
                weekStart = bundle.schedule.weekStart
            )
            if (isThisWeekCourse || showNotThisWeekCourse) {
                val fromWeekNum = courseTime.getFromWeekNum(weekNum)
                if (fromWeekNum != null) {
                    replaceList.clear()
                    val weekNumDiff = weekNum - fromWeekNum
                    var isIntersectWithThisWeekCourse = false
                    var needReplace = true
                    for (cell in resultSet) {
                        if (cell.sectionTime intersect courseTime.sectionTime) {
                            if (isThisWeekCourse && !cell.isThisWeekCourse) {
                                replaceList.add(cell)
                                continue
                            }
                            if (!isThisWeekCourse && cell.isThisWeekCourse) {
                                isIntersectWithThisWeekCourse = true
                                needReplace = false
                                break
                            }
                            val cellWeekNumDiff = weekNum - cell.fromWeekNum
                            if (
                                (weekNumDiff > 0 && cellWeekNumDiff > 0 && weekNumDiff < cellWeekNumDiff) ||
                                (weekNumDiff > 0 && cellWeekNumDiff <= 0) ||
                                (weekNumDiff <= 0 && cellWeekNumDiff <= 0 && weekNumDiff > cellWeekNumDiff)
                            ) {
                                replaceList.add(cell)
                            } else {
                                needReplace = false
                                break
                            }
                        }
                    }
                    if (!isIntersectWithThisWeekCourse && needReplace) {
                        if (replaceList.isNotEmpty()) resultSet.removeAll(replaceList)
                        resultSet.add(CourseCell(course, courseTime, fromWeekNum, isThisWeekCourse))
                    }
                }
            }
        }

        return ScheduleViewData(weekNum, bundle.schedule, resultSet.toList(), bundle.scheduleStyles)
    }

    fun getMaxWeekNum(courses: List<Course>): Int {
        var defaultValue = 1
        courses.iterateAll { _, courseTime ->
            courseTime.weekNumArray = courseTime.weekNumArray.arrangeWeekNum()
            defaultValue = max(defaultValue, courseTime.weekNumArray.size)
        }
        return defaultValue
    }

    fun hasWeekendCourse(cells: List<CourseCell>): Boolean {
        for (cell in cells) {
            if (cell.sectionTime.weekDay.isWeekend) {
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
            if (it.sectionTime.end > scheduleTimes.size) CourseAdapterException.Error.MAX_COURSE_NUM_ERROR.report()
        }

        allTimes.forEachTwo { _, t1, _, t2 ->
            if (t1 intersect t2) {
                foundConflicts = true
                t2.weekNumArray = BooleanArray(0)
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