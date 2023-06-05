package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.Course.Companion.arrangeWeekNum
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.content.utils.CourseParseResult
import java.io.Serializable
import java.util.Date

abstract class AbstractSimpleCourseParser<P : Serializable, T> : AbstractCourseParser<P>() {
    fun parseScheduleTimes(importOption: Int, content: T) = onParseScheduleTimes(importOption, content)

    fun parseCourses(importOption: Int, content: T) = onParseCourses(importOption, content).also {
        it.courses.arrangeWeekNum()
    }

    fun parseTerm(importOption: Int, content: T) = onParseTerm(importOption, content)

    protected abstract fun onParseScheduleTimes(importOption: Int, content: T): List<ScheduleTime>

    protected abstract fun onParseCourses(importOption: Int, content: T): CourseParseResult

    protected open fun onParseTerm(importOption: Int, content: T): Pair<Date, Date>? = null
}