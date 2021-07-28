@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import tool.xfy9326.schedule.beans.TimePeriod
import tool.xfy9326.schedule.kt.isEven
import tool.xfy9326.schedule.kt.isOdd
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

object CourseAdapterUtils {

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

    fun newDateFormat(format: String = "yyyy-MM-dd"): SimpleDateFormat {
        return SimpleDateFormat(format, Locale.getDefault())
    }

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
        val sortedArr = arr.toSet().sorted().toList()
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
}