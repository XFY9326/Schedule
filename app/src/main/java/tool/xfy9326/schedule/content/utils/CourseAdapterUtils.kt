@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.TimePeriod
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.kt.isEven
import tool.xfy9326.schedule.kt.isOdd
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

object CourseAdapterUtils {

    /**
     * 构建简单的Http客户端
     *
     * @param supportJson 是否支持JSON
     * @param hasRedirect 是否允许重定向
     * @return HttpClient
     */
    fun buildSimpleHttpClient(supportJson: Boolean = false, hasRedirect: Boolean = true) = HttpClient(OkHttp) {
        install(HttpCookies)
        if (hasRedirect) {
            install(HttpRedirect) {
                // 修复 Http 302 Post 错误
                checkHttpMethod = false
            }
        }
        if (supportJson) {
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }
        BrowserUserAgent()
    }

    /**
     * 创建日期格式
     *
     * @param format 格式
     * @return SimpleDateFormat
     */
    fun newDateFormat(format: String = "yyyy-MM-dd"): SimpleDateFormat {
        return SimpleDateFormat(format, Locale.getDefault())
    }

    /**
     * 简单的学期时间修复
     * 适用于学期时间有可能只有开头的情况
     *
     * @param termStart 学期开始时间
     * @param termEnd 学期结束时间
     * @return Pair<学期开始时间, 学期结束时间>
     */
    fun simpleTermFix(termStart: Date?, termEnd: Date?) =
        if (termStart != null && termEnd == null) {
            termStart to termStart
        } else if (termStart != null && termEnd != null) {
            termStart to termEnd
        } else {
            null
        }

    /**
     * 整数数字间隔解析
     * 输入示例：5  1,2-3,4
     *
     * @param str 需要解析的字符串
     * @param groupDivider 组分割符号
     * @param durationDivider 时间段分割符号
     * @param oddOnly 仅限偶数
     * @param evenOnly 仅限奇数
     * @return 解析结果数组
     */
    fun parseNumberPeriods(str: String, groupDivider: String = ",", durationDivider: String = "-", oddOnly: Boolean = false, evenOnly: Boolean = false): BooleanArray {
        var maxValue = 0
        val groups = str.split(groupDivider).map {
            val duration = it.split(durationDivider).map { num -> num.trim().toInt() }
            val fixedDuration = if (duration.size == 1) {
                duration[0] to duration[0]
            } else {
                var min = duration[0]
                var max = duration[0]
                duration.forEach { num ->
                    min = min(num, min)
                    max = max(num, max)
                }
                min to max
            }
            maxValue = max(fixedDuration.second, maxValue)
            return@map fixedDuration
        }
        val result = BooleanArray(maxValue)
        groups.forEach {
            for (num in it.first..it.second) {
                if ((oddOnly == evenOnly) || (oddOnly && num.isOdd()) || (evenOnly && num.isEven())) {
                    result[num - 1] = true
                }
            }
        }
        return result
    }

    /**
     * 将整形Collection转为时间间隔数组
     *
     * @param arr 整形Collection
     * @return 时间间隔数组
     */
    fun parseIntCollectionPeriod(arr: Collection<Int>): List<TimePeriod> {
        val sortedArr = (if (arr is Set<Int>) arr else arr.toSet()).toList().sorted()
        if (sortedArr.isNotEmpty()) {
            val result = ArrayList<TimePeriod>()
            var start = 0
            for ((i, num) in sortedArr.withIndex()) {
                if (i == 0) {
                    start = num
                } else {
                    if (sortedArr[i - 1] != num - 1) {
                        result.add(TimePeriod(start, sortedArr[i - 1]))
                        start = num
                    }
                }
            }
            val last = sortedArr.last()
            if (last - start >= 0) {
                result.add(TimePeriod(start, last))
            }

            return result
        } else {
            return emptyList()
        }
    }

    /**
     * 解析多个课程时间
     * 用于上课时间并非完整的时间片段（例如：1-3）而是只包含节次（例如：1，2，3，5）的课程
     *
     * @param weeks 周数
     * @param weekDay 星期
     * @param numArr 上课时间
     * @param location 地点
     * @return 课程时间
     */
    fun parseMultiCourseTimes(weeks: BooleanArray, weekDay: WeekDay, numArr: Collection<Int>, location: String? = null): List<CourseTime> {
        val timePeriods = parseIntCollectionPeriod(numArr)
        val result = ArrayList<CourseTime>(timePeriods.size)
        for (period in timePeriods) {
            result.add(CourseTime(weeks, weekDay, period.start, period.length, location))
        }
        return result
    }
}