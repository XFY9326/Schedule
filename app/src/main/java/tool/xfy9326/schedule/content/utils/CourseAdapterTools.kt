@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import androidx.annotation.IntRange
import org.jsoup.nodes.Element

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
fun Collection<Int>.toBooleanArray(): BooleanArray {
    val max = maxOrNull()
    return if (max == null) {
        BooleanArray(0)
    } else {
        BooleanArray(max).also {
            for (i in this) {
                it[i - 1] = true
            }
        }
    }
}

fun Element.selectSingle(cssQuery: String) = selectFirst(cssQuery) ?: throw NoSuchElementException("No element found by css selector! $cssQuery")