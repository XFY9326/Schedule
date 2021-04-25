package tool.xfy9326.schedule.content.adapters.parser

import tool.xfy9326.schedule.content.base.CourseParseResult
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report

class NAUCourseWebParser : WebCourseParser<Nothing>() {
    companion object {
        private const val PAGE_TEXT = "在修课程课表"
    }

    private val loginParser = NAUCourseParser()

    override fun onParseScheduleTimes(importOption: Int, htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>) =
        loginParser.parseScheduleTimes(importOption)

    override fun onParseTerm(importOption: Int, htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>) =
        loginParser.parseTerm(importOption, htmlContent)

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