package tool.xfy9326.schedule.content.adapters.parser

import tool.xfy9326.schedule.beans.WebPageContent
import tool.xfy9326.schedule.content.base.CourseParseResult
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report

class NAUCourseWebParser : WebCourseParser<Nothing>() {
    private val loginParser = NAUCourseParser()

    override fun onParseScheduleTimes(importOption: Int, webPageContent: WebPageContent) =
        loginParser.parseScheduleTimes(importOption)

    override fun onParseTerm(importOption: Int, webPageContent: WebPageContent) =
        loginParser.parseTerm(importOption, webPageContent.htmlContent)

    override fun onParseCourses(importOption: Int, webPageContent: WebPageContent): CourseParseResult {
        if (webPageContent.providedContent != null) {
            return loginParser.parseCourses(importOption, webPageContent.providedContent)
        } else {
            CourseAdapterException.Error.PARSER_ERROR.report()
        }
    }
}