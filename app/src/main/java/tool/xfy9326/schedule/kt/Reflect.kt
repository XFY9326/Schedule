@file:Suppress("unused", "UNCHECKED_CAST")

package tool.xfy9326.schedule.kt

fun <T> Any.cast() = this as T

fun <T> Any.tryCast() = this as? T

fun <T> Any?.castNullable() = this as T?

fun <T> Any?.castNonNull() = this as T