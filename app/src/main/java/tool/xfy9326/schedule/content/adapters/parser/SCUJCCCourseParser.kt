package tool.xfy9326.schedule.content.adapters.parser

import lib.xfy9326.kit.nullIfBlank
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils
import tool.xfy9326.schedule.content.utils.CourseParseResult

class SCUJCCCourseParser : WebCourseParser<Nothing>() {
    companion object {
        private const val TAG_BR = "<br>"
        private const val COURSE_DIVIDER_OPTION0 = "$TAG_BR$TAG_BR"
        private const val COURSE_DIVIDER_OPTION1 = "$TAG_BR$TAG_BR$TAG_BR"
        private const val COURSE_SELECTOR = "tr > td:has(> br)"
        private const val ODD_WEEK_STR = "单"
        private const val EVEN_WEEK_STR = "双"

        const val IMPORT_OPTION_TABLE_1 = 0
        const val IMPORT_OPTION_TABLE_6 = 1
    }

    // 无法在安卓设备上使用 (?:) 表达式
    // 不可以在安卓设备上省略 } 的转译
    @Suppress("RegExpRedundantEscape")
    private val courseTimeOption0Reg = "周(.*?)第(.*?)节\\{第(.*?)周(\\|(.*?)周)?\\}".toRegex()
    private val courseTimeOption1Reg = "(.*?)\\((.*?)\\)".toRegex()

    override fun onParseScheduleTimes(importOption: Int, content: WebPageContent): List<ScheduleTime> {
        return ScheduleTime.listOf(
            8, 15, 9, 0,
            9, 5, 9, 50,
            10, 10, 10, 55,
            11, 0, 11, 45,
            14, 0, 14, 45,
            14, 50, 15, 35,
            15, 55, 16, 40,
            16, 45, 17, 30,
            19, 0, 19, 45,
            19, 50, 20, 35,
            20, 40, 21, 25,
            21, 30, 22, 15
        )
    }

    override fun onParseCourses(importOption: Int, content: WebPageContent): CourseParseResult {
        return parseCourses(importOption, Jsoup.parseBodyFragment(content.requireProvidedContent()).body())
    }

    private fun parseCourses(importOption: Int, element: Element): CourseParseResult {
        val column = element.select(COURSE_SELECTOR)
        if (column.isEmpty()) return CourseParseResult.EMPTY

        val builder = CourseParseResult.Builder()

        for (data in column) {
            when (importOption) {
                IMPORT_OPTION_TABLE_1 -> parseCourseOption0(builder, data)
                IMPORT_OPTION_TABLE_6 -> parseCourseOption1(builder, data)
                else -> CourseAdapterException.Error.IMPORT_SELECT_OPTION_ERROR.report()
            }
        }

        return builder.build(combineCourse = true, combineCourseTime = true)
    }

    private fun parseCourseOption0(builder: CourseParseResult.Builder, data: Element) {
        val courseData = data.html().split(COURSE_DIVIDER_OPTION0)
        for (courseDatum in courseData) {
            builder.withCatcher {
                val details = courseDatum.split(TAG_BR)

                if (details.size < 4) {
                    CourseAdapterException.Error.INCOMPLETE_COURSE_INFO_ERROR.report()
                } else {
                    val name = details[0].trim()
                    if (name.isBlank()) CourseAdapterException.Error.INCOMPLETE_COURSE_INFO_ERROR.report()
                    val teacher = details[2].trim().nullIfBlank()
                    val courseTimes = parseTimeOption0(details)
                    builder.add(Course(name, teacher, courseTimes))
                }
            }
        }
    }

    private fun parseTimeOption0(details: List<String>): List<CourseTime> {
        val courseTimeData = courseTimeOption0Reg.matchEntire(details[1].trim())?.groups ?: CourseAdapterException.Error.CONTENT_PARSE_ERROR.report()
        val weekDayStr = courseTimeData[1]?.value?.trim() ?: CourseAdapterException.Error.CONTENT_PARSE_ERROR.report()
        val sectionTimeStr = courseTimeData[2]?.value?.trim() ?: CourseAdapterException.Error.CONTENT_PARSE_ERROR.report()
        val weekNumStr = courseTimeData[3]?.value?.trim() ?: CourseAdapterException.Error.CONTENT_PARSE_ERROR.report()
        val weekModeStr = courseTimeData[5]?.value?.trim() ?: CourseAdapterException.Error.CONTENT_PARSE_ERROR.report()

        val weekDay = CourseAdapterUtils.parseWeekDayChinese(weekDayStr)
        val sectionTimes = CourseAdapterUtils.parseNumberList(sectionTimeStr)
        val weekMode = CourseAdapterUtils.parseWeekModeChinese(weekModeStr)
        val weekNum = CourseAdapterUtils.parseWeekNum(weekNumStr, oddOnly = weekMode.first, evenOnly = weekMode.second)
        return CourseAdapterUtils.parseMultiCourseTimes(weekNum, weekDay, sectionTimes, details[3].trim().nullIfBlank())
    }

    private fun parseCourseOption1(builder: CourseParseResult.Builder, data: Element) {
        val datumRow = data.parent()?.elementSiblingIndex() ?: return
        if (datumRow == 0) return

        val datumColumn = data.elementSiblingIndex()

        val weekDay = WeekDay.of(if (datumRow == 2 || datumRow == 6 || datumRow == 10) (datumColumn - 1) else datumColumn)

        val courseData = data.html().split(COURSE_DIVIDER_OPTION1)

        for (courseDatum in courseData) {
            builder.withCatcher {
                val details = courseDatum.split(TAG_BR)

                if (details.size < 4) {
                    CourseAdapterException.Error.INCOMPLETE_COURSE_INFO_ERROR.report()
                } else {
                    val name = details[0].trim()
                    if (name.isBlank()) CourseAdapterException.Error.INCOMPLETE_COURSE_INFO_ERROR.report()
                    val teacher = details[2].trim().nullIfBlank()
                    val courseTimes = parseTimeOption1(weekDay, details)
                    builder.add(Course(name, teacher, courseTimes))
                }
            }
        }
    }

    private fun parseTimeOption1(weekDay: WeekDay, details: List<String>): List<CourseTime> {
        val courseTimeData = courseTimeOption1Reg.matchEntire(details[1].trim())?.groups ?: CourseAdapterException.Error.CONTENT_PARSE_ERROR.report()
        var weeksStr = courseTimeData[1]?.value?.trim() ?: CourseAdapterException.Error.CONTENT_PARSE_ERROR.report()
        val sectionTimeStr = courseTimeData[2]?.value?.trim() ?: CourseAdapterException.Error.CONTENT_PARSE_ERROR.report()

        val oddMode = weeksStr.endsWith(ODD_WEEK_STR)
        if (oddMode) {
            weeksStr = weeksStr.substring(0, weeksStr.length - ODD_WEEK_STR.length)
        }
        val evenMode = weeksStr.endsWith(EVEN_WEEK_STR)
        if (evenMode) {
            weeksStr = weeksStr.substring(0, weeksStr.length - EVEN_WEEK_STR.length)
        }

        val weekNum = CourseAdapterUtils.parseWeekNum(weeksStr, oddOnly = oddMode, evenOnly = evenMode)
        val sectionTimes = CourseAdapterUtils.parseNumberList(sectionTimeStr)
        return CourseAdapterUtils.parseMultiCourseTimes(weekNum, weekDay, sectionTimes, details[3].trim().nullIfBlank())
    }
}