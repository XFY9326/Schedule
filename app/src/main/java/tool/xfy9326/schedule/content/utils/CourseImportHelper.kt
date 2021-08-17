package tool.xfy9326.schedule.content.utils

import tool.xfy9326.schedule.beans.NetworkCourseImportParams
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.beans.WebPageContent
import tool.xfy9326.schedule.content.base.*

object CourseImportHelper {

    suspend fun importNetworkCourse(
        params: NetworkCourseImportParams,
        importOption: Int,
        provider: NetworkCourseProvider<*>,
        parser: NetworkCourseParser<*>,
    ): ScheduleImportContent {
        if (provider is LoginCourseProvider) {
            provider.login(params.userId, params.userPw, params.captchaCode, importOption)
        }

        val scheduleTimesHtml = provider.loadScheduleTimesHtml(importOption)
        val coursesHtml = provider.loadCoursesHtml(importOption)
        val termHtml = provider.loadTermHtml(importOption)

        val scheduleTimes = parser.parseScheduleTimes(importOption, scheduleTimesHtml)
        val coursesParseResult = parser.parseCourses(importOption, coursesHtml)
        val term = parser.parseTerm(importOption, termHtml)

        return ScheduleImportContent(scheduleTimes, coursesParseResult, term)
    }

    fun analyseWebPage(content: WebPageContent, provider: WebCourseProvider<*>): Pair<Int, WebPageContent>? {
        val pageInfo = provider.validateCourseImportPage(content.htmlContent, content.iframeContent, content.frameContent)
        return if (pageInfo.isValidPage) {
            pageInfo.asImportOption to content.copy(providedContent = pageInfo.providedContent)
        } else {
            null
        }
    }

    fun parseWebCourse(
        content: WebPageContent,
        importOption: Int,
        parser: WebCourseParser<*>,
    ): ScheduleImportContent {
        val scheduleTimes = parser.parseScheduleTimes(importOption, content)
        val coursesParseResult = parser.parseCourses(importOption, content)
        val term = parser.parseTerm(importOption, content)
        return ScheduleImportContent(scheduleTimes, coursesParseResult, term)
    }
}