package tool.xfy9326.schedule.content.adapters.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.content.base.CourseParseResult
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils
import tool.xfy9326.schedule.kt.nullIfBlank

class SCUJCCCourseParser : WebCourseParser<Nothing>() {
    companion object {
        private const val TAG_BR = "<br>"
        private const val COURSE_DIVIDER = "$TAG_BR$TAG_BR$TAG_BR"
        private const val COURSE_SELECTOR = "tbody > tr > td:has(> br)"
        private const val ODD_WEEK_STR = "单"
        private const val EVEN_WEEK_STR = "双"
        private const val COURSE_TIME_DIVIDER = ","
    }

    private data class CourseInfo(val name: String, val teacher: String? = null)

    private data class CourseTimeInfo(
        val weeks: BooleanArray,
        val weekDay: WeekDay,
        val location: String? = null,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CourseTimeInfo) return false

            if (!weeks.contentEquals(other.weeks)) return false
            if (weekDay != other.weekDay) return false
            if (location != other.location) return false

            return true
        }

        override fun hashCode(): Int {
            var result = weeks.contentHashCode()
            result = 31 * result + weekDay.hashCode()
            result = 31 * result + (location?.hashCode() ?: 0)
            return result
        }
    }

    private val courseTimeReg = "(.*?)\\((.*?)\\)".toRegex()

    override fun onParseScheduleTimes(importOption: Int, webPageContent: WebPageContent): List<ScheduleTime> {
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

    override fun onParseCourses(importOption: Int, webPageContent: WebPageContent): CourseParseResult {
        if (webPageContent.providedContent != null) {
            return parseCourses(Jsoup.parseBodyFragment(webPageContent.providedContent).body())
        } else {
            CourseAdapterException.Error.PARSER_ERROR.report()
        }
    }

    private fun parseCourses(element: Element): CourseParseResult {
        val builder = CourseParseResult.Builder()

        val courseInfoMap = HashMap<CourseInfo, HashMap<CourseTimeInfo, HashSet<Int>>>()

        val column = element.select(COURSE_SELECTOR)
        for (data in column) {
            val datumRow = data.parent()?.elementSiblingIndex() ?: continue
            if (datumRow == 0) continue

            builder.withCatcher {
                val datumColumn = data.elementSiblingIndex()

                val weekDay = WeekDay.of(if (datumRow == 2 || datumRow == 6 || datumRow == 10) (datumColumn - 1) else datumColumn)

                val courseData = data.html().split(COURSE_DIVIDER)

                for (courseDatum in courseData) {
                    val details = courseDatum.split(TAG_BR)

                    if (details.size < 4) {
                        CourseAdapterException.Error.INCOMPLETE_COURSE_INFO_ERROR.report()
                    } else {
                        val name = details[0].trim()
                        if (name.isBlank()) CourseAdapterException.Error.INCOMPLETE_COURSE_INFO_ERROR.report()
                        val teacher = details[2].trim().nullIfBlank()
                        val timeInfo = parseTime(weekDay, details)
                        val courseInfo = CourseInfo(name, teacher)

                        val courseTimeMap = courseInfoMap.getOrPut(courseInfo) { HashMap() }
                        val timeSet = courseTimeMap.getOrPut(timeInfo.first) { HashSet() }
                        timeSet.addAll(timeInfo.second)
                    }
                }
            }
        }

        return courseInfoMapToResult(builder, courseInfoMap)
    }

    private fun courseInfoMapToResult(builder: CourseParseResult.Builder, courseInfoMap: HashMap<CourseInfo, HashMap<CourseTimeInfo, HashSet<Int>>>): CourseParseResult {
        for (courseEntry in courseInfoMap) {
            val course = courseEntry.key
            val courseTimes = courseEntry.value
            val courseTimeList = ArrayList<CourseTime>()
            for (timesEntry in courseTimes) {
                val courseTime = timesEntry.key
                val timePeriods = CourseAdapterUtils.parseIntCollectionPeriod(timesEntry.value)
                for (timePeriod in timePeriods) {
                    courseTimeList.add(CourseTime(courseTime.weeks, courseTime.weekDay, timePeriod.start, timePeriod.length, courseTime.location))
                }
            }
            builder.add(Course(course.name, course.teacher, courseTimeList))
        }
        return builder.build()
    }

    private fun parseTime(weekDay: WeekDay, details: List<String>): Pair<CourseTimeInfo, List<Int>> {
        val courseTimeData = courseTimeReg.matchEntire(details[1].trim())?.groups ?: CourseAdapterException.Error.PARSER_ERROR.report()
        var weeksStr = courseTimeData[1]?.value?.trim() ?: CourseAdapterException.Error.PARSER_ERROR.report()
        val courseTimeStr = courseTimeData[2]?.value?.trim() ?: CourseAdapterException.Error.PARSER_ERROR.report()

        val oddMode = weeksStr.endsWith(ODD_WEEK_STR)
        if (oddMode) {
            weeksStr = weeksStr.substring(0, weeksStr.length - ODD_WEEK_STR.length)
        }
        val evenMode = weeksStr.endsWith(EVEN_WEEK_STR)
        if (evenMode) {
            weeksStr = weeksStr.substring(0, weeksStr.length - EVEN_WEEK_STR.length)
        }
        val weeks = CourseAdapterUtils.parseNumberPeriods(weeksStr, oddOnly = oddMode, evenOnly = evenMode)

        val times = courseTimeStr.split(COURSE_TIME_DIVIDER).map { it.trim().toInt() }

        return CourseTimeInfo(weeks, weekDay, details[3].trim().nullIfBlank()) to times
    }
}