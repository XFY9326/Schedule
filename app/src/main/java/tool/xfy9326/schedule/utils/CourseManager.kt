package tool.xfy9326.schedule.utils

import android.graphics.Bitmap
import android.view.View
import androidx.annotation.Px
import androidx.core.graphics.applyCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.io.GlobalIO
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.view.ScheduleView
import kotlin.math.max

object CourseManager {
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
        firstDayOfWeek: WeekDay,
    ) =
        if (courseTime.hasThisWeekCourse(weekNum)) {
            when (weekNum) {
                1 -> startWeekDay.value(firstDayOfWeek) <= courseTime.classTime.weekDay.value(firstDayOfWeek)
                maxWeekNum -> endWeekDay.value(firstDayOfWeek) >= courseTime.classTime.weekDay.value(firstDayOfWeek)
                else -> true
            }
        } else {
            false
        }

    fun getMaxWeekNum(courses: List<Course>): Int {
        var defaultValue = 1
        courses.iterateAll { _, courseTime ->
            courseTime.weekNum = courseTime.weekNum.fit()
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
        Course(0, scheduleId, GlobalIO.resources.getString(R.string.new_course), null, MaterialColorHelper.random(), listOf(createNewCourseTime()))

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

    suspend fun generateScheduleImageByWeekNum(scheduleId: Long, weekNum: Int, @Px targetWidth: Int) =
        withContext(Dispatchers.Default) {
            val schedule = ScheduleDBProvider.db.scheduleDAO.getSchedule(scheduleId).firstOrNull() ?: return@withContext null
            val courses = ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(scheduleId).first()
            val styles = ScheduleDataStore.scheduleStylesFlow.firstOrNull()?.copy(
                viewAlpha = 100,
                timeTextColor = null,
                cornerScreenMargin = false
            ) ?: return@withContext null

            val backgroundColor = App.instance.getDefaultBackgroundColor()
            val scheduleView = ScheduleView(App.instance, getScheduleViewDataByWeek(weekNum, ScheduleBuildBundle(schedule, courses, styles)))

            val widthSpec = View.MeasureSpec.makeMeasureSpec(targetWidth, View.MeasureSpec.AT_MOST)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            scheduleView.measure(widthSpec, heightSpec)
            scheduleView.layout(0, 0, scheduleView.measuredWidth, scheduleView.measuredHeight)
            scheduleView.requestLayout()

            return@withContext Bitmap.createBitmap(scheduleView.measuredWidth, scheduleView.measuredHeight, Bitmap.Config.ARGB_8888).applyCanvas {
                drawColor(backgroundColor)
                scheduleView.draw(this)
            }
        }
}