@file:Suppress("unused", "UNCHECKED_CAST")

package tool.xfy9326.schedule.kt

import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

fun <T> Any.cast() = this as T

fun <T> Any.tryCast() = this as? T

fun <T> Any?.castNullable() = this as T?

fun <T> Any?.castNonNull() = this as T

fun <T> KClass<*>.getSuperGenericTypeClass(i: Int) =
    this.java.genericSuperclass.castNonNull<ParameterizedType>().actualTypeArguments[i].cast<Class<T>>()