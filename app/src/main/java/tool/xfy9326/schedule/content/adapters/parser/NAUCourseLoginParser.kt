package tool.xfy9326.schedule.content.adapters.parser

import org.jsoup.Jsoup
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.content.base.NetworkCourseParser
import tool.xfy9326.schedule.kt.fit
import tool.xfy9326.schedule.kt.isEven
import tool.xfy9326.schedule.kt.isOdd

class NAUCourseLoginParser : NetworkCourseParser() {
    companion object {
        private const val INIT_WEEK_NUM = 20
        private const val COURSE_TR_TAGS = "#content > tbody > tr[align='center']"
        private val WEEKDAY_COURSE_REG = "周 (\\d+) 第 (\\d+)-(\\d+)节".toRegex()

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

            return weeks.fit()
        }

        private fun getCourseTime(location: String, timeStr: String): CourseTime? {
            val weeks = getWeeks(timeStr)

            val weekDayCourseSectionStr = timeStr.substring(timeStr.indexOf("周") + 1).trim()
            val weekDayCourseValues = WEEKDAY_COURSE_REG.matchEntire(weekDayCourseSectionStr)?.groupValues ?: return null

            val weekDay = weekDayCourseValues[1].toInt()
            val classStart = weekDayCourseValues[2].toInt()
            val classEnd = weekDayCourseValues[3].toInt()

            return CourseTime(weeks, WeekDay.of(weekDay), classStart, classEnd - classStart + 1, location)
        }
    }

    override fun onParseScheduleTimes(importOption: Int, htmlContent: String?) =
        arrayOf(
            ScheduleTime(8, 30, 9, 10),
            ScheduleTime(9, 20, 10, 0),
            ScheduleTime(10, 20, 11, 0),
            ScheduleTime(11, 10, 11, 50),
            ScheduleTime(12, 0, 12, 40),
            ScheduleTime(13, 30, 14, 10),
            ScheduleTime(14, 20, 15, 0),
            ScheduleTime(15, 20, 16, 0),
            ScheduleTime(16, 10, 16, 50),
            ScheduleTime(17, 0, 17, 40),
            ScheduleTime(18, 30, 19, 10),
            ScheduleTime(19, 20, 20, 0),
            ScheduleTime(20, 10, 20, 50)
        )

    override fun onParseCourses(importOption: Int, htmlContent: String): Array<Course> {
        val body = Jsoup.parse(htmlContent).body()
        val trTags = body.select(COURSE_TR_TAGS)
        val result = ArrayList<Course>(trTags.size)
        for (trTag in trTags) {
            val values = trTag.children()
            val courseName = values[2].text()
            val courseTeacher = values[7].text()

            val timeStr = values[8].text().trim()

            if ("上课地点：" !in timeStr || timeStr.isEmpty()) continue

            val timeStrArr = timeStr.split("上课地点：")
            val courseTimes = ArrayList<CourseTime>(timeStrArr.size - 1)

            for (i in 1 until timeStrArr.size) {
                val times = timeStrArr[i].split("上课时间：")
                getCourseTime(times[0].trim(), times[1].trim())?.let {
                    courseTimes.add(it)
                }
            }

            result.add(Course(courseName, courseTeacher, courseTimes))
        }

        return result.toTypedArray()
    }
}