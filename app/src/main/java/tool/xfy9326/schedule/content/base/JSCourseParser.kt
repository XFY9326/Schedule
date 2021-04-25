package tool.xfy9326.schedule.content.base

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.beans.JSParams
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.json.parser.ai.AiScheduleResult
import tool.xfy9326.schedule.json.parser.ai.CourseInfos
import tool.xfy9326.schedule.json.parser.pure.ScheduleImportJSON
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

    private val termDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
        val courseNum = info.sections.asSequence().map { it.section }.toSet().sorted().toList()
        if (courseNum.isNotEmpty()) {
            val weekDay = WeekDay.of(info.day)

            val result = ArrayList<ClassTime>()
            var start = 0
            for ((i, num) in courseNum.withIndex()) {
                if (i == 0) {
                    start = num
                } else {
                    if (courseNum[i - 1] + 1 != num) {
                        result.add(ClassTime(weekDay, start, courseNum[i - 1] - start + 1))
                        start = num
                    }
                }
            }
            val last = courseNum.last()
            if (last - start >= 0) {
                result.add(ClassTime(weekDay, start, last - start + 1))
            }

            return result
        } else {
            return emptyList()
        }
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
                Course(course.name, course.teacher, course.times.map {
                    CourseTime(getWeeksArray(it.weekNum), WeekDay.of(it.weekDay), it.start, it.duration, it.location)
                })
            }
        }

        val termStart = scheduleData.termStart?.let { termDateFormat.parse(it) }
        val termEnd = scheduleData.termEnd?.let { termDateFormat.parse(it) }
        val term = if (termStart == null && termEnd != null) {
            termEnd to termEnd
        } else if (termStart != null && termEnd == null) {
            termStart to termStart
        } else if (termStart != null && termEnd != null) {
            termStart to termEnd
        } else {
            null
        }

        return ScheduleImportContent(scheduleTimes, builder.build(), term)
    }
}