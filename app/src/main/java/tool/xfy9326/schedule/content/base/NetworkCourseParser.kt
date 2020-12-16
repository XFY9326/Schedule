@file:Suppress("unused")

package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleTime

abstract class NetworkCourseParser : ICourseParser {
    fun parseScheduleTimes(importOption: Int, htmlContent: String? = null) = onParseScheduleTimes(importOption, htmlContent)

    fun parseCourses(importOption: Int, htmlContent: String) = onParseCourses(importOption, htmlContent)

    protected abstract fun onParseScheduleTimes(importOption: Int, htmlContent: String?): Array<ScheduleTime>

    protected abstract fun onParseCourses(importOption: Int, htmlContent: String): Array<Course>
}