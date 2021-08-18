package tool.xfy9326.schedule.content.utils

import tool.xfy9326.schedule.beans.NetworkCourseImportParams
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.beans.WebPageContent
import tool.xfy9326.schedule.content.base.*

object CourseImportHelper {

    // 导入在线课程
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

    // 分析网页
    // 返回：导入选项，网页分析结果
    // 返回null代表网页内容无法解析出结果
    fun analyseWebPage(content: WebPageContent, provider: WebCourseProvider<*>): Pair<Int, WebPageContent>? {
        val pageInfo = provider.validateCourseImportPage(content.htmlContent, content.iframeContent, content.frameContent)
        return if (pageInfo.isValidPage) {
            pageInfo.asImportOption to content.copy(providedContent = pageInfo.providedContent)
        } else {
            null
        }
    }

    // 解析网页
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