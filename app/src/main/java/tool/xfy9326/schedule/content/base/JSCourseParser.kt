package tool.xfy9326.schedule.content.base

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.beans.JSParams
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils
import tool.xfy9326.schedule.json.parser.ai.AiScheduleResult
import tool.xfy9326.schedule.json.parser.ai.CourseInfos
import tool.xfy9326.schedule.json.parser.pure.ScheduleImportJSON

class JSCourseParser : AbstractCourseParser<JSParams>() {
    companion object {
        private fun getWeeksArray(weeks: List<Int>): BooleanArray {
            val fixedWeeks = weeks.toSet().toList()
            val max = fixedWeeks.maxOrNull()
            return if (max == null) {
                BooleanArray(0)
            } else {
                BooleanArray(max) {
                    (it + 1) in fixedWeeks
                }
            }
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun processJSResult(data: String) =
        try {
            when (requireParams().jsType) {
                JSConfig.TYPE_AI_SCHEDULE -> processAiScheduleResult(data)
                JSConfig.TYPE_PURE_SCHEDULE -> processPureScheduleResult(data)
                else -> error("Unsupported JS Type! ${requireParams().jsType}")
            }
        } catch (e: Exception) {
            CourseAdapterException.Error.PARSER_ERROR.report(e)
        }

    private fun processAiScheduleResult(data: String): ScheduleImportContent {
        val scheduleData = json.decodeFromString<AiScheduleResult>(data)

        val scheduleTimes = ArrayList<ScheduleTime>(scheduleData.sectionTimes.size)
        scheduleData.sectionTimes.sortedBy {
            it.section
        }.forEach {
            scheduleTimes.add(ScheduleTime.fromTimeStr(it.startTime, it.endTime))
        }

        val builder = CourseParseResult.Builder(scheduleData.courseInfos.size)

        for (info in scheduleData.courseInfos) {
            val classTimes = getSeriesClassTimeList(info)
            for (classTime in classTimes) {
                builder.add(combineSameCourse = true) {
                    val courseTime = CourseTime(getWeeksArray(info.weeks), classTime, info.position?.takeIf { it.isNotBlank() })
                    Course(info.name, info.teacher?.takeIf { it.isNotBlank() }, listOf(courseTime))
                }
            }
        }

        return ScheduleImportContent(scheduleTimes, builder.build())
    }

    private fun getSeriesClassTimeList(info: CourseInfos): List<ClassTime> {
        val timePeriods = CourseAdapterUtils.parseIntCollectionPeriod(info.sections.map { it.section })
        if (timePeriods.isEmpty()) return emptyList()

        val weekDay = WeekDay.of(info.day)
        val result = ArrayList<ClassTime>(timePeriods.size)
        for (timePeriod in timePeriods) {
            result.add(ClassTime(weekDay, timePeriod.start, timePeriod.length))
        }

        return result
    }

    private fun processPureScheduleResult(data: String): ScheduleImportContent {
        val scheduleData = json.decodeFromString<ScheduleImportJSON>(data)

        val scheduleTimes = ArrayList<ScheduleTime>(scheduleData.times.size)
        scheduleData.times.forEach {
            scheduleTimes.add(it.toScheduleTime())
        }

        val builder = CourseParseResult.Builder(scheduleData.courses.size)

        for (course in scheduleData.courses) {
            builder.add {
                Course(course.name.trim(), course.teacher?.trim(), course.times.map {
                    CourseTime(getWeeksArray(it.weekNum), it.weekDay, it.start, it.duration, it.location?.trim())
                })
            }
        }

        return ScheduleImportContent(scheduleTimes, builder.build(), CourseAdapterUtils.simpleTermFix(scheduleData.termStart, scheduleData.termEnd))
    }
}