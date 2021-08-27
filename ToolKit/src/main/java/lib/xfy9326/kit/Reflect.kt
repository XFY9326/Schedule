@file:Suppress("unused", "UNCHECKED_CAST")

package lib.xfy9326.kit

fun <T> Any.cast() = this as T

fun <T> Any.tryCast() = this as? T

fun <T> Any?.castNullable() = this as T?

fun <T> Any?.castNonNull() = this as T