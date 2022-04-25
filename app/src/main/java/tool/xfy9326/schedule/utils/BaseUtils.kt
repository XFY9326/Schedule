package tool.xfy9326.schedule.utils

import io.github.xfy9326.atools.core.tryEnumValueOf
import kotlin.math.max
import kotlin.math.min

const val CHAR_ZERO = '0'
const val CHAR_ONE = '1'

const val NEW_LINE = "\n"

inline fun <T> List<T>.forEachTwo(action: (Int, T, Int, T) -> Unit) {
    for (i1 in indices) for (i2 in (i1 + 1)..lastIndex) action(i1, this[i1], i2, this[i2])
}

// O(3/2n)
fun List<Int>.minAndMax(): Pair<Int, Int>? {
    if (size == 0) return null
    if (size == 1) return this[0] to this[0]
    if (size == 2) return if (this[0] < this[1]) this[0] to this[1] else this[1] to this[0]
    var minValue: Int
    var maxValue: Int
    if (this[0] < this[1]) {
        minValue = this[0]
        maxValue = this[1]
    } else {
        minValue = this[1]
        maxValue = this[0]
    }

    var i = 2
    while (i < size) {
        if (i == size - 1) {
            if (this[i] < minValue) {
                minValue = this[i]
            } else if (this[i] > maxValue) {
                maxValue = this[i]
            }
            break
        } else {
            if (this[i] < this[i + 1]) {
                minValue = min(this[i], minValue)
                maxValue = max(this[i + 1], maxValue)
            } else {
                minValue = min(this[i + 1], minValue)
                maxValue = max(this[i], maxValue)
            }
        }
        i += 2
    }

    return minValue to maxValue
}

fun Throwable.getDeepStackTraceString() = cause?.stackTraceToString() ?: stackTraceToString()

inline fun <reified E : Enum<E>> tryEnumSetValueOf(names: Set<String>): Set<E> {
    return names.mapNotNull {
        tryEnumValueOf<E>(it)
    }.toSet()
}

fun BooleanArray.serializeToString(): String {
    return buildString(size) {
        this@serializeToString.forEach { b ->
            append(if (b) CHAR_ONE else CHAR_ZERO)
        }
    }
}

fun String.deserializeToBooleanArray(): BooleanArray {
    return BooleanArray(length) { p ->
        this[p] == CHAR_ONE
    }
}