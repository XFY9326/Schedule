package tool.xfy9326.schedule.content.adapters.parser

import tool.xfy9326.schedule.beans.WebPageContent
import tool.xfy9326.schedule.content.base.CourseParseResult
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report

class NAUCourseWebParser : WebCourseParser<Nothing>() {
    private val loginParser = NAUCourseParser()

    override fun onParseScheduleTimes(importOption: Int, content: WebPageContent) =
        loginParser.parseScheduleTimes(importOption, null)

    override fun onParseTerm(importOption: Int, content: WebPageContent) =
        loginParser.parseTerm(importOption, content.htmlContent)

    override fun onParseCourses(importOption: Int, content: WebPageContent): CourseParseResult {
        if (content.providedContent != null) {
            return loginParser.parseCourses(importOption, content.providedContent)
        } else {
            CourseAdapterException.Error.PARSER_ERROR.report()
        }
    }
}