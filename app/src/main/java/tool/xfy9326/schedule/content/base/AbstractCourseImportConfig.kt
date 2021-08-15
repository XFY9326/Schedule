@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.base

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import lib.xfy9326.android.kit.io.IOManager
import lib.xfy9326.kit.clone
import tool.xfy9326.schedule.content.beans.CourseImportInstance
import java.io.Serializable
import java.util.*
import kotlin.reflect.KClass

/**
 * 课程导入配置
 * 当前可用：
 * LoginCourseProvider -- NetworkCourseParser
 * NetworkCourseProvider -- NetworkCourseParser
 * WebCourseProvider -- WebCourseParser
 * JSCourseProvider -- JSCourseParser  仅限内部使用
 *
 */
abstract class AbstractCourseImportConfig<P1 : Serializable, T1 : AbstractCourseProvider<P1>, P2 : Serializable, T2 : AbstractCourseParser<P2>>(
    override val schoolName: String,
    override val authorName: String,
    override val systemName: String,
    private val staticImportOptions: Array<String>? = null,
    private val providerClass: Class<T1>,
    private val parserClass: Class<T2>,
    private val providerParams: P1? = null,
    private val parserParams: P2? = null,
    sortingBasis: String,
) : ICourseImportConfig, Serializable {
    constructor(
        @StringRes schoolNameResId: Int,
        @StringRes authorNameResId: Int,
        @StringRes systemNameResId: Int,
        @ArrayRes staticImportOptionsResId: Int? = null,
        providerClass: KClass<T1>,
        parserClass: KClass<T2>,
        providerParams: P1? = null,
        parserParams: P2? = null,
        sortingBasis: String,
    ) : this(
        IOManager.resources.getString(schoolNameResId),
        IOManager.resources.getString(authorNameResId),
        IOManager.resources.getString(systemNameResId),
        staticImportOptionsResId?.let { IOManager.resources.getStringArray(it) },
        providerClass.java,
        parserClass.java,
        providerParams,
        parserParams,
        sortingBasis
    )

    @Transient
    override val lowerCaseSortingBasis = sortingBasis.lowercase(Locale.getDefault())

    fun isProviderType(clazz: KClass<out AbstractCourseProvider<*>>) = clazz.java.isAssignableFrom(providerClass)

    fun isParserType(clazz: KClass<out AbstractCourseParser<*>>) = clazz.java.isAssignableFrom(parserClass)

    fun getInstance() = IOManager.resources.run {
        CourseImportInstance(
            schoolName,
            authorName,
            systemName,
            staticImportOptions,
            createProviderInstance(),
            createParserInstance()
        )
    }

    private fun createProviderInstance() = providerClass.newInstance().apply {
        setParams(providerParams?.clone())
    }

    private fun createParserInstance() = parserClass.newInstance().apply {
        setParams(parserParams?.clone())
    }

    override fun toString(): String {
        return "AbstractCourseImportConfig(schoolName='$schoolName', authorName='$authorName', systemName='$systemName', staticImportOptions=${staticImportOptions?.contentToString()}, providerClass=$providerClass, parserClass=$parserClass, providerParams=$providerParams, parserParams=$parserParams)"
    }
}