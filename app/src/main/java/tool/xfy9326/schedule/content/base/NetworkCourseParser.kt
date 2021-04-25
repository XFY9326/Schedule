package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.content.utils.arrangeWeekNum
import java.io.Serializable
import java.util.*

abstract class NetworkCourseParser<P : Serializable> : AbstractCourseParser<P>() {
    fun parseScheduleTimes(importOption: Int, htmlContent: String? = null) = onParseScheduleTimes(importOption, htmlContent)

    fun parseCourses(importOption: Int, htmlContent: String) = onParseCourses(importOption, htmlContent).also {
        it.courses.arrangeWeekNum()
    }

    fun parseTerm(importOption: Int, htmlContent: String? = null) = onParseTerm(importOption, htmlContent)

    protected abstract fun onParseScheduleTimes(importOption: Int, htmlContent: String?): List<ScheduleTime>

    protected abstract fun onParseCourses(importOption: Int, htmlContent: String): CourseParseResult

    protected open fun onParseTerm(importOption: Int, htmlContent: String?): Pair<Date, Date>? = null
}