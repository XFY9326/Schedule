@file:Suppress("unused")

package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleTime

/**
 * Network course parser
 *
 * @constructor Create empty Network course parser
 */
abstract class NetworkCourseParser : ICourseParser {
    fun parseScheduleTimes(importOption: Int, htmlContent: String? = null) = onParseScheduleTimes(importOption, htmlContent)

    fun parseCourses(importOption: Int, htmlContent: String) = onParseCourses(importOption, htmlContent)

    /**
     * Parse schedule times
     *
     * @param importOption Import option, Default: 0
     * @param htmlContent Html content
     * @return Schedule time list
     */
    protected abstract fun onParseScheduleTimes(importOption: Int, htmlContent: String?): Array<ScheduleTime>

    /**
     * Parse courses
     *
     * @param importOption Import option, Default: 0
     * @param htmlContent Html content
     * @return Course Array
     */
    protected abstract fun onParseCourses(importOption: Int, htmlContent: String): Array<Course>
}