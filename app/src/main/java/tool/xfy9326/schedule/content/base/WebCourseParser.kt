package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.content.utils.arrangeWeekNum
import java.io.Serializable
import java.util.*

abstract class WebCourseParser<P : Serializable> : AbstractCourseParser<P>() {

    fun parseScheduleTimes(importOption: Int, htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>) =
        onParseScheduleTimes(importOption, htmlContent, iframeContent, frameContent)

    fun parseCourses(importOption: Int, htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>) =
        onParseCourses(importOption, htmlContent, iframeContent, frameContent).also {
            it.courses.arrangeWeekNum()
        }

    fun parseTerm(importOption: Int, htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>) =
        onParseTerm(importOption, htmlContent, iframeContent, frameContent)

    protected abstract fun onParseScheduleTimes(
        importOption: Int,
        htmlContent: String,
        iframeContent: Array<String>,
        frameContent: Array<String>,
    ): List<ScheduleTime>

    protected abstract fun onParseCourses(
        importOption: Int,
        htmlContent: String,
        iframeContent: Array<String>,
        frameContent: Array<String>,
    ): CourseParseResult

    protected open fun onParseTerm(
        importOption: Int,
        htmlContent: String,
        iframeContent: Array<String>,
        frameContent: Array<String>,
    ): Pair<Date, Date>? = null
}