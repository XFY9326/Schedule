package tool.xfy9326.schedule.content.adapters.parser

import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.content.base.CourseParseResult
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.utils.CourseAdapterException

class NAUCourseWebParser : WebCourseParser<Nothing>() {
    companion object {
        private const val PAGE_TEXT = "在修课程课表"
    }

    private val loginParser = NAUCourseParser()

    override fun onLoadScheduleTimes(importOption: Int): List<ScheduleTime> = loginParser.parseScheduleTimes(importOption)

    override fun onParseCourses(
        importOption: Int,
        htmlContent: String,
        iframeContent: Array<String>,
        frameContent: Array<String>,
    ): CourseParseResult {
        for (content in iframeContent) {
            if (PAGE_TEXT in content) return loginParser.parseCourses(importOption, content)
        }
        CourseAdapterException.Error.PARSER_ERROR.report()
    }
}