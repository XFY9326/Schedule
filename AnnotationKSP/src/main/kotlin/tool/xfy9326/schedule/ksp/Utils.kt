package tool.xfy9326.schedule.ksp

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import tool.xfy9326.schedule.ksp.base.AnnotatedClass
import kotlin.reflect.KClass

const val PackageName = "tool.xfy9326.schedule"

val KOTLIN_LIST = List::class.asClassName()
val KOTLIN_MAP = Map::class.asClassName()
val KOTLIN_CLASS = KClass::class.asClassName()
val KOTLIN_COROUTINES_WITH_CONTEXT = MemberName("kotlinx.coroutines", "withContext")
val KOTLIN_COROUTINES_DISPATCHERS_IO = MemberName("kotlinx.coroutines.Dispatchers", "IO")

fun createParametersString(size: Int, codeStr: String) = Array(size) { codeStr }.joinToString()

fun Collection<AnnotatedClass<*>>.toClassNames(): Array<ClassName> =
    map { it.ksClassDeclaration.toClassName() }.toTypedArray()