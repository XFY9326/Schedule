package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleTime

/**
 * Web course parser
 *
 * @constructor Create empty Web course parser
 */
abstract class WebCourseParser : ICourseParser {

    fun loadScheduleTimes(importOption: Int) = onLoadScheduleTimes(importOption)


    fun parseCourses(importOption: Int, htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>) =
        onParseCourses(importOption, htmlContent, iframeContent, frameContent)

    /**
     * Load schedule times
     *
     * @param importOption Import option, Default: 0
     * @return
     */
    protected abstract fun onLoadScheduleTimes(importOption: Int): Array<ScheduleTime>

    /**
     * Parse courses
     *
     * @param importOption Import option, Default: 0
     * @param htmlContent Courses main html
     * @param iframeContent Courses iframeContent html array
     * @param frameContent Courses frameContent html array
     * @return
     */
    protected abstract fun onParseCourses(
        importOption: Int,
        htmlContent: String,
        iframeContent: Array<String>,
        frameContent: Array<String>,
    ): Array<Course>
}