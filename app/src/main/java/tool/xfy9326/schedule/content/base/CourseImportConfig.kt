@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.base

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import tool.xfy9326.schedule.content.utils.clone
import java.io.Serializable
import java.util.*
import kotlin.reflect.KClass

/**
 * 课程导入配置
 * 当前可用：
 * LoginCourseProvider -- NetworkCourseParser
 * NetworkCourseProvider -- NetworkCourseParser
 * WebCourseProvider -- WebCourseParser
 *
 * KClass不可序列化，此处只能使用Java的Class
 */
abstract class CourseImportConfig<P1 : Serializable, T1 : AbstractCourseProvider<P1>, P2 : Serializable, T2 : AbstractCourseParser<P2>>(
    @StringRes
    val schoolNameResId: Int,
    @StringRes
    val authorNameResId: Int,
    @StringRes
    val systemNameResId: Int,
    @ArrayRes
    val staticImportOptionsResId: Int? = null,
    private val providerClass: Class<T1>,
    private val parserClass: Class<T2>,
    private val providerParams: P1? = null,
    private val parserParams: P2? = null,
    sortingBasis: String,
) : Serializable {
    val lowerCaseSortingBasis = sortingBasis.toLowerCase(Locale.getDefault())

    fun newProvider(): T1 = providerClass.newInstance().also {
        it.params = providerParams?.clone()
    }

    fun newParser(): T2 = parserClass.newInstance().also {
        it.params = parserParams?.clone()
    }

    fun validateProviderType(clazz: KClass<out AbstractCourseProvider<*>>) = clazz.java.isAssignableFrom(providerClass)

    fun validateParserType(clazz: KClass<out AbstractCourseParser<*>>) = clazz.java.isAssignableFrom(parserClass)
}