package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.Course.Companion.arrangeWeekNum
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.WebPageContent
import java.io.Serializable
import java.util.*

abstract class WebCourseParser<P : Serializable> : AbstractCourseParser<P>() {

    fun parseScheduleTimes(importOption: Int, webPageContent: WebPageContent) = onParseScheduleTimes(importOption, webPageContent)

    fun parseCourses(importOption: Int, webPageContent: WebPageContent) = onParseCourses(importOption, webPageContent).also {
        it.courses.arrangeWeekNum()
    }

    fun parseTerm(importOption: Int, webPageContent: WebPageContent) = onParseTerm(importOption, webPageContent)

    protected abstract fun onParseScheduleTimes(importOption: Int, webPageContent: WebPageContent): List<ScheduleTime>

    protected abstract fun onParseCourses(importOption: Int, webPageContent: WebPageContent): CourseParseResult

    protected open fun onParseTerm(importOption: Int, webPageContent: WebPageContent): Pair<Date, Date>? = null
}