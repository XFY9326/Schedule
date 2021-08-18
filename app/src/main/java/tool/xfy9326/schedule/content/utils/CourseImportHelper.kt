package tool.xfy9326.schedule.content.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.xfy9326.kit.nullIfBlank
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.content.base.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.json.parser.pure.ScheduleImportJSON

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

    // 解析Pure课程表JSON文件
    fun parsePureScheduleJSON(data: String, combineCourse: Boolean, combineCourseTime: Boolean): ScheduleImportContent {
        val json = Json { ignoreUnknownKeys = true }
        val scheduleData = try {
            json.decodeFromString<ScheduleImportJSON>(data)
        } catch (e: Exception) {
            CourseAdapterException.Error.JSON_PARSE_ERROR.report(e)
        }

        val scheduleTimes = ArrayList<ScheduleTime>(scheduleData.times.size)
        scheduleData.times.forEach {
            scheduleTimes.add(it.toScheduleTime())
        }

        val builder = CourseParseResult.Builder(scheduleData.courses.size)

        for (course in scheduleData.courses) {
            builder.add {
                val times = ArrayList<CourseTime>()
                for (time in course.times) {
                    val weekNum = time.weekNum.toBooleanArray()
                    val location = time.location?.trim().nullIfBlank()
                    if (time.start != null && time.duration != null) {
                        times.add(CourseTime(weekNum, time.weekDay, time.start, time.duration, location))
                    } else if (time.sections != null) {
                        times.addAll(CourseAdapterUtils.parseMultiCourseTimes(weekNum, time.weekDay, time.sections, location))
                    } else {
                        CourseAdapterException.Error.JSON_PARSE_ERROR.report()
                    }
                }
                Course(course.name.trim(), course.teacher?.trim().nullIfBlank(), times)
            }
        }

        return ScheduleImportContent(
            scheduleTimes,
            builder.build(combineCourse, combineCourseTime),
            CourseAdapterUtils.simpleTermFix(scheduleData.termStart, scheduleData.termEnd)
        )
    }
}