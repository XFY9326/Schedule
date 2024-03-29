package tool.xfy9326.schedule.content.js

import io.github.xfy9326.atools.base.nullIfBlank
import kotlinx.serialization.json.Json
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.beans.JSParams
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils
import tool.xfy9326.schedule.content.utils.CourseImportHelper
import tool.xfy9326.schedule.content.utils.CourseParseResult
import tool.xfy9326.schedule.content.utils.toBooleanArray
import tool.xfy9326.schedule.json.parser.ai.AiScheduleResult

class JSCourseParser : AbstractCourseParser<JSParams>() {

    fun processJSResult(data: String) =
        try {
            when (requireParams().jsType) {
                JSConfig.TYPE_AI_SCHEDULE -> processAiScheduleResult(data)
                JSConfig.TYPE_PURE_SCHEDULE -> CourseImportHelper.parsePureScheduleJSON(data, requireParams().parseParams)
                else -> error("Unsupported JS Type! ${requireParams().jsType}")
            }
        } catch (e: Exception) {
            CourseAdapterException.Error.JS_RESULT_PARSE_ERROR.report(e)
        }

    private fun processAiScheduleResult(data: String): ScheduleImportContent {
        val json = Json { ignoreUnknownKeys = true }
        val scheduleData = try {
            json.decodeFromString<AiScheduleResult>(data)
        } catch (e: Exception) {
            CourseAdapterException.Error.JSON_PARSE_ERROR.report(e)
        }

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
                val courseTimes = CourseAdapterUtils.parseMultiCourseTimes(
                    weeks,
                    WeekDay.of(info.day),
                    info.sections,
                    info.position?.nullIfBlank()?.trim()
                )
                Course(info.name.trim(), info.teacher?.nullIfBlank()?.trim(), courseTimes)
            }
        }

        return ScheduleImportContent(
            scheduleTimes,
            builder.build(requireParams().parseParams),
            CourseAdapterUtils.simpleTermFix(scheduleData.pureSchedule?.termStart, scheduleData.pureSchedule?.termEnd)
        )
    }
}