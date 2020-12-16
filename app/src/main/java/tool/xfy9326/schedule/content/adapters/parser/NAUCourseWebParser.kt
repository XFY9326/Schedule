package tool.xfy9326.schedule.content.adapters.parser

import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.utils.CourseAdapterException

class NAUCourseWebParser : WebCourseParser() {
    companion object {
        private const val PAGE_TEXT = "在修课程课表"
    }

    private val loginParser = NAUCourseLoginParser()

    override fun onLoadScheduleTimes(importOption: Int): Array<ScheduleTime> = loginParser.parseScheduleTimes(importOption)

    override fun onParseCourses(
        importOption: Int,
        htmlContent: String,
        iframeContent: Array<String>,
        frameContent: Array<String>,
    ): Array<Course> {
        for (content in iframeContent) {
            if (PAGE_TEXT in content) return loginParser.parseCourses(importOption, content)
        }
        CourseAdapterException.ErrorType.PARSER_ERROR.report()
    }
}