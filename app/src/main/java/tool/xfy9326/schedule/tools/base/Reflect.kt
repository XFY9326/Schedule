@file:Suppress("unused", "UNCHECKED_CAST", "NOTHING_TO_INLINE")

package tool.xfy9326.schedule.tools.base

inline fun <T> Any.cast() = this as T

inline fun <T> Any.tryCast() = this as? T

inline fun <T> Any?.castNullable() = this as T?

inline fun <T> Any?.castNonNull() = this as T