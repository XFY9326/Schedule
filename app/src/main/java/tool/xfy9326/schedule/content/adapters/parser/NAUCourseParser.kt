package tool.xfy9326.schedule.content.adapters.parser

import org.jsoup.Jsoup
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.content.adapters.provider.NAUJwcCourseProvider
import tool.xfy9326.schedule.content.base.CourseParseResult
import tool.xfy9326.schedule.content.base.NetworkCourseParser
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils
import tool.xfy9326.schedule.kt.isEven
import tool.xfy9326.schedule.kt.isOdd
import java.util.*
import kotlin.collections.ArrayList

class NAUCourseParser : NetworkCourseParser<Nothing>() {
    companion object {
        private const val INIT_WEEK_NUM = 24
        private const val TERM_START_SELECTOR = "#TermInfo > div:nth-child(4) > span"
        private const val TERM_END_SELECTOR = "#TermInfo > div:nth-child(5) > span"
        private const val COURSE_TR_TAGS = "#content > tbody > tr[align='center']"
        private val WEEKDAY_COURSE_REG = "周\\s*(\\d+)\\s*第\\s*(\\d+)-(\\d+)节".toRegex()

        private fun getWeeks(timeStr: String): BooleanArray {
            // 周数模式（0: 任意周  1: 单周  2: 双周）
            var weekMode = 0
            val weekNumStr: String

            val weeksStr = timeStr.substring(0, timeStr.indexOf("周") + 1).trim()
            if (weeksStr.startsWith("第")) {
                weekNumStr = weeksStr.substring(1, weeksStr.length - 1).trim()
            } else if ("之" in weeksStr) {
                weekNumStr = weeksStr.substring(0, weeksStr.indexOf("之")).trim()
                if ("单" in weeksStr) {
                    weekMode = 1
                } else if ("双" in weeksStr) {
                    weekMode = 2
                }
            } else {
                weekNumStr = weeksStr.substring(0, weeksStr.length - 1).trim()
            }

            val weeks = BooleanArray(INIT_WEEK_NUM)
            val weekNumStrArr =
                if ("," in weekNumStr) {
                    weekNumStr.split(",")
                } else {
                    listOf(weekNumStr)
                }
            for (str in weekNumStrArr) {
                val trimStr = str.trim()
                if ("-" in trimStr) {
                    val numStrArr = trimStr.split("-")
                    for (num in numStrArr[0].toInt()..numStrArr[1].toInt()) {
                        when (weekMode) {
                            0 -> weeks[num - 1] = true
                            1 -> if (num.isOdd()) weeks[num - 1] = true
                            2 -> if (num.isEven()) weeks[num - 1] = true
                        }
                    }
                } else {
                    weeks[trimStr.toInt() - 1] = true
                }
            }

            return weeks
        }

        private fun getCourseTime(location: String, timeStr: String): CourseTime? {
            val weeks = getWeeks(timeStr)

            val weekDayCourseSectionStr = timeStr.substring(timeStr.indexOf("周") + 1).trim()
            val weekDayCourseValues = WEEKDAY_COURSE_REG.find(weekDayCourseSectionStr)?.groupValues ?: return null

            val weekDay = weekDayCourseValues[1].toInt()
            val classStart = weekDayCourseValues[2].toInt()
            val classEnd = weekDayCourseValues[3].toInt()

            return CourseTime(weeks, WeekDay.of(weekDay), classStart, classEnd - classStart + 1, location)
        }
    }

    private val termDateFormat = CourseAdapterUtils.newDateFormat()

    override fun onParseScheduleTimes(importOption: Int, htmlContent: String?) =
        ScheduleTime.listOf(
            8, 30, 9, 10,
            9, 20, 10, 0,
            10, 20, 11, 0,
            11, 10, 11, 50,
            12, 0, 12, 40,
            13, 30, 14, 10,
            14, 20, 15, 0,
            15, 20, 16, 0,
            16, 10, 16, 50,
            17, 0, 17, 40,
            18, 30, 19, 10,
            19, 20, 20, 0,
            20, 10, 20, 50
        )

    override fun onParseCourses(importOption: Int, htmlContent: String): CourseParseResult {
        val body = Jsoup.parse(htmlContent).body()
        val trTags = body.select(COURSE_TR_TAGS)

        val builder = CourseParseResult.Builder(trTags.size)

        for (trTag in trTags) {
            builder.add {
                val values = trTag.children()
                val courseName = values[2].text()
                val courseTeacher = values[7].text()

                val timeStr = values[8].text().trim()

                if ("上课地点：" !in timeStr || timeStr.isEmpty()) {
                    null
                } else {
                    val timeStrArr = timeStr.split("上课地点：")
                    val courseTimes = ArrayList<CourseTime>(timeStrArr.size - 1)

                    for (i in 1 until timeStrArr.size) {
                        val times = timeStrArr[i].split("上课时间：")
                        getCourseTime(times[0].trim(), times[1].trim())?.let {
                            courseTimes.add(it)
                        }
                    }

                    Course(courseName, courseTeacher, courseTimes)
                }
            }
        }

        return builder.build()
    }

    override fun onParseTerm(importOption: Int, htmlContent: String?): Pair<Date, Date>? {
        if (importOption == NAUJwcCourseProvider.IMPORT_OPTION_THIS_TERM && htmlContent != null) {
            try {
                val body = Jsoup.parse(htmlContent).body()
                val termStartStr = body.selectFirst(TERM_START_SELECTOR).text().trim()
                val termEndStr = body.selectFirst(TERM_END_SELECTOR).text().trim()
                val termStart = termDateFormat.parse(termStartStr)
                val termEnd = termDateFormat.parse(termEndStr)
                if (termStart != null && termEnd != null) {
                    return termStart to termEnd
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }
}