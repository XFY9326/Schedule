@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import androidx.annotation.IntRange
import io.ktor.client.*
import io.ktor.client.features.*
import org.jsoup.nodes.Element
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig

typealias BaseCourseImportConfig = AbstractCourseImportConfig<*, *, *, *>

/**
 * 重新整理周数数组
 * 减少不必要的尾部信息
 * @return 周数数组
 */
fun BooleanArray.arrangeWeekNum(): BooleanArray {
    var newSize = size
    for (i in size - 1 downTo 0) {
        if (this[i]) {
            break
        } else {
            newSize = i
        }
    }
    return when (newSize) {
        0 -> BooleanArray(0)
        size -> this
        else -> copyOfRange(0, newSize)
    }
}

/**
 * 是否有课程
 *
 * @param num 节次（从1开始）
 * @return 是否有课程
 */
fun BooleanArray.hasCourse(@IntRange(from = 1) num: Int): Boolean {
    val index = num - 1
    return if (index in indices) {
        this[index]
    } else {
        false
    }
}

/**
 * 整形Collection转BooleanArray
 * 1 -> Index 0
 * 2 -> Index 1
 *
 * @return BooleanArray
 */
fun Collection<Int>.toBooleanArray(): BooleanArray =
    BooleanArray(maxOrNull() ?: 0).also {
        for (i in this) {
            it[i - 1] = true
        }
    }

/**
 * Jsoup选择单个元素，若不存在则报错
 *
 * @param cssQuery CSS查询语句
 */
fun Element.selectSingle(cssQuery: String): Element =
    selectFirst(cssQuery) ?: throw NoSuchElementException("No element found by css selector! $cssQuery\n Html:\n${outerHtml()}")

/**
 * 带有响应错误处理的HttpClient
 * Ktor默认会抛出网络请求状态码错误，因此可能需要特殊处理
 *
 * @param T 返回的内容类型
 * @param action 网络请求
 * @param handleError 处理错误
 * @return 返回的内容
 */
suspend fun <T> HttpClient.runResponseCatching(
    action: suspend HttpClient.() -> T,
    handleError: suspend ResponseException.() -> T,
): T = runCatching {
    action()
}.getOrElse {
    if (it is ResponseException) {
        handleError(it)
    } else {
        throw it
    }
}