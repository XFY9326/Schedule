package tool.xfy9326.schedule.content.js

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.xfy9326.kit.nullIfBlank
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.base.CourseParseResult
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.beans.JSParams
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils
import tool.xfy9326.schedule.content.utils.CourseImportHelper
import tool.xfy9326.schedule.content.utils.toBooleanArray
import tool.xfy9326.schedule.json.parser.ai.AiScheduleResult

class JSCourseParser : AbstractCourseParser<JSParams>() {

    fun processJSResult(data: String) =
        try {
            when (requireParams().jsType) {
                JSConfig.TYPE_AI_SCHEDULE -> processAiScheduleResult(data)
                JSConfig.TYPE_PURE_SCHEDULE -> CourseImportHelper.parsePureScheduleJSON(data, requireParams().combineCourse, requireParams().combineCourseTime)
                else -> error("Unsupported JS Type! ${requireParams().jsType}")
            }
        } catch (e: Exception) {
            CourseAdapterException.Error.PARSER_ERROR.report(e)
        }

    private fun processAiScheduleResult(data: String): ScheduleImportContent {
        val json = Json { ignoreUnknownKeys = true }
        val scheduleData = json.decodeFromString<AiScheduleResult>(data)

        val scheduleTimes = ArrayList<ScheduleTime>(scheduleData.sectionTimes.size)
        scheduleData.sectionTimes.sortedBy {
            it.section
        }.forEach {
            scheduleTimes.add(ScheduleTime.fromTimeStr(it.startTime, it.endTime))
        }

        val builder = CourseParseResult.Builder(scheduleData.courseInfos.size)

        for (info in scheduleData.courseInfos) {
            builder.add {
                val weeks = info.weeks.toBooleanArray()
                val courseTimes = CourseAdapterUtils.parseMultiCourseTimes(weeks, WeekDay.of(info.day), info.sections.map { it.section }, info.position?.nullIfBlank())
                Course(info.name, info.teacher?.nullIfBlank(), courseTimes)
            }
        }

        return ScheduleImportContent(scheduleTimes, builder.build(requireParams().combineCourse, requireParams().combineCourseTime))
    }
}