package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleTime

abstract class WebCourseParser : ICourseParser {
    fun loadScheduleTimes(importOption: Int) = onLoadScheduleTimes(importOption)

    fun parseCourses(importOption: Int, htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>) =
        onParseCourses(importOption, htmlContent, iframeContent, frameContent)

    protected abstract fun onLoadScheduleTimes(importOption: Int): Array<ScheduleTime>

    protected abstract fun onParseCourses(
        importOption: Int,
        htmlContent: String,
        iframeContent: Array<String>,
        frameContent: Array<String>,
    ): Array<Course>
}