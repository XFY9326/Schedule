@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.utils

import io.github.xfy9326.atools.base.isEven
import io.github.xfy9326.atools.base.isOdd
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.TimePeriod
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.utils.minAndMax
import java.text.SimpleDateFormat
import java.util.*

object CourseAdapterUtils {

    /**
     * 构建简单的Http客户端
     *
     * @param supportJson 是否支持JSON（Ktor对Kotlin serialization的支持）
     * @param hasRedirect 是否允许重定向
     * @param autoRetry 网络错误时自动重试
     * @param cookiesStorage Cookie存储器
     * @param engineConfiguration 网络引擎配置
     * @return HttpClient
     */
    fun buildSimpleHttpClient(
        supportJson: Boolean = false,
        hasRedirect: Boolean = true,
        autoRetry: Boolean = true,
        unsafeRedirect: Boolean = false,
        cookiesStorage: CookiesStorage = AcceptAllCookiesStorage(),
        engineConfiguration: (OkHttpConfig.() -> Unit)? = null
    ): HttpClient = HttpClient(OkHttp) {
        BrowserUserAgent()
        engineConfiguration?.let(::engine)
        if (autoRetry) {
            install(HttpRequestRetry)
        }
        install(HttpCookies) {
            storage = cookiesStorage
        }
        if (hasRedirect) {
            install(HttpRedirect) {
                if (unsafeRedirect) {
                    checkHttpMethod = false
                    allowHttpsDowngrade = true
                }
            }
        }
        if (supportJson) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                    }
                )
            }
        }
    }

    /**
     * 创建日期格式解析
     *
     * @param format 日期格式
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
    fun simpleTermFix(termStart: Date?, termEnd: Date?): Pair<Date, Date>? =
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
     * @return 整形集合
     */
    fun parseNumberPeriods(str: String, groupDivider: String = ",", durationDivider: String = "-", oddOnly: Boolean = false, evenOnly: Boolean = false): Set<Int> {
        val result = HashSet<Int>()
        if (str.isBlank()) return result
        str.split(groupDivider).mapNotNull {
            it.split(durationDivider).map { num -> num.trim().toInt() }.minAndMax()
        }.forEach {
            for (num in it.first..it.second) {
                if ((oddOnly == evenOnly) || (oddOnly && num.isOdd()) || (evenOnly && num.isEven())) {
                    result.add(num)
                }
            }
        }
        return result
    }

    /**
     * 整数数字列表解析
     * 输入示例：1,2,4,5
     *
     * @param str 字符串
     * @param divider 分隔符
     * @return 整形列表
     */
    fun parseNumberList(str: String, divider: String = ","): List<Int> = str.split(divider).map { it.trim().toInt() }

    // 用于周数解析
    fun parseWeekNum(str: String, groupDivider: String = ",", durationDivider: String = "-", oddOnly: Boolean = false, evenOnly: Boolean = false): BooleanArray =
        parseNumberPeriods(str, groupDivider, durationDivider, oddOnly, evenOnly).toBooleanArray()

    /**
     * 将整形Collection转为时间间隔数组
     *
     * @param arr 整形Collection
     * @return 时间间隔数组
     */
    fun parseIntCollectionPeriod(arr: Collection<Int>): List<TimePeriod> {
        if (arr.isEmpty()) return emptyList()
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
        if (numArr.isEmpty()) return emptyList()
        val timePeriods = parseIntCollectionPeriod(numArr)
        val result = ArrayList<CourseTime>(timePeriods.size)
        for (period in timePeriods) {
            result.add(CourseTime(weeks, weekDay, period.start, period.length, location))
        }
        return result
    }

    /**
     * 解析中文的星期
     *
     * @param str 中文星期
     * @return WeekDay
     */
    fun parseWeekDayChinese(str: String): WeekDay {
        val fixedStr = when {
            str.isBlank() -> error("Blank input!")
            str.startsWith("周") -> str.trim().substring(1)
            str.startsWith("星期") -> str.trim().substring(2)
            else -> str
        }.trim()
        return when (fixedStr) {
            "一" -> WeekDay.MONDAY
            "二" -> WeekDay.TUESDAY
            "三" -> WeekDay.WEDNESDAY
            "四" -> WeekDay.THURSDAY
            "五" -> WeekDay.FRIDAY
            "六" -> WeekDay.SATURDAY
            "天", "日" -> WeekDay.SUNDAY
            else -> error("Unsupported WeekDay Chinese! Input: $str")
        }
    }

    /**
     * 解析中文单双周
     *
     * @param str 中文'单'或'双'或'单周'或'双周'
     * @return 是否是单周 to 是否是双周
     */
    fun parseWeekModeChinese(str: String?): Pair<Boolean, Boolean> =
        when (str?.trim()) {
            "单", "单周" -> true to false
            "双", "双周" -> false to true
            else -> false to false
        }
}