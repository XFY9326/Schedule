package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.content.utils.arrangeWeekNum
import java.io.Serializable

abstract class WebCourseParser<P : Serializable> : AbstractCourseParser<P>() {

    fun loadScheduleTimes(importOption: Int) = onLoadScheduleTimes(importOption)

    fun parseCourses(importOption: Int, htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>) =
        onParseCourses(importOption, htmlContent, iframeContent, frameContent).also {
            it.courses.arrangeWeekNum()
        }

    protected abstract fun onLoadScheduleTimes(importOption: Int): List<ScheduleTime>

    protected abstract fun onParseCourses(
        importOption: Int,
        htmlContent: String,
        iframeContent: Array<String>,
        frameContent: Array<String>,
    ): CourseParseResult
}