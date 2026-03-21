package tool.xfy9326.schedule.tools.base

const val EMPTY = ""

fun <T : CharSequence> T?.nullIfBlank(): T? = if (isNullOrBlank()) null else this

inline fun <reified T> T.asList(): List<T> = listOf(this)

inline fun <reified T> T.asArray(): Array<T> = arrayOf(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.isOdd(): Boolean = this % 2 != 0

@Suppress("NOTHING_TO_INLINE")
inline fun Int.isEven(): Boolean = this % 2 == 0
