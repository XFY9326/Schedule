@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.base

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import tool.xfy9326.schedule.content.beans.CourseImportInstance
import tool.xfy9326.schedule.content.utils.clone
import tool.xfy9326.schedule.io.IOManager
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
 */
abstract class AbstractCourseImportConfig<P1 : Serializable, T1 : AbstractCourseProvider<P1>, P2 : Serializable, T2 : AbstractCourseParser<P2>>(
    @StringRes
    val schoolNameResId: Int,
    @StringRes
    val authorNameResId: Int,
    @StringRes
    val systemNameResId: Int,
    @ArrayRes
    val staticImportOptionsResId: Int? = null,
    private val providerClass: KClass<T1>,
    private val parserClass: KClass<T2>,
    private val providerParams: P1? = null,
    private val parserParams: P2? = null,
    sortingBasis: String,
) {
    val lowerCaseSortingBasis = sortingBasis.toLowerCase(Locale.getDefault())

    fun validateProviderType(clazz: KClass<out AbstractCourseProvider<*>>) = clazz.java.isAssignableFrom(providerClass.java)

    fun validateParserType(clazz: KClass<out AbstractCourseParser<*>>) = clazz.java.isAssignableFrom(parserClass.java)

    fun getInstance() = IOManager.resources.run {
        CourseImportInstance(
            getString(schoolNameResId),
            getString(authorNameResId),
            getString(systemNameResId),
            staticImportOptionsResId?.let { getStringArray(it) },
            newProvider(),
            newParser()
        )
    }

    private fun newProvider(): T1 = providerClass.java.newInstance().also {
        it.params = providerParams?.clone()
    }

    private fun newParser(): T2 = parserClass.java.newInstance().also {
        it.params = parserParams?.clone()
    }
}