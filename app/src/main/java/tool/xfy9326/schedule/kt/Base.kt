@file:Suppress("unused")

package tool.xfy9326.schedule.kt

import kotlinx.coroutines.sync.Mutex

const val NEW_LINE = "\n"

const val APP_ID = "PureSchedule"

fun Int.isOdd(): Boolean = this % 2 != 0

fun Int.isEven(): Boolean = this % 2 == 0

inline fun <reified E : Enum<E>> tryEnumValueOf(name: String?): E? {
    return if (name == null) {
        null
    } else {
        try {
            enumValueOf<E>(name)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

inline fun <reified E : Enum<E>> tryEnumValueOf(names: Set<String>?): Set<E>? {
    return if (names == null) {
        null
    } else {
        HashSet<E>(names.size).apply {
            names.forEach {
                tryEnumValueOf<E>(it)?.let(::add)
            }
        }
    }
}

inline fun <T> List<T>.forEachTwo(action: (Int, T, Int, T) -> Unit) {
    for (i1 in indices) for (i2 in (i1 + 1)..lastIndex) action(i1, this[i1], i2, this[i2])
}

inline fun <T> Mutex.withTryLock(owner: Any? = null, action: () -> T): T? {
    if (tryLock(owner)) {
        try {
            return action()
        } finally {
            unlock(owner)
        }
    }
    return null
}

fun Throwable.getDeepStackTraceString() = cause?.stackTraceToString() ?: stackTraceToString()

fun <T : CharSequence> T.nullIfBlank() = if (isBlank()) null else this