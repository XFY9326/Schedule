@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.base

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import tool.xfy9326.schedule.kt.cast
import tool.xfy9326.schedule.kt.clone
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * 课程导入配置
 * 当前可用：
 * LoginCourseProvider -- NetworkCourseParser
 * NetworkCourseProvider -- NetworkCourseParser
 * WebCourseProvider -- WebCourseParser
 *
 * 注：由于泛型，无法使用Parcelable类型传递数据
 *
 * @param P Serializable
 * @param T1 CourseProvider
 * @param T2 CourseParser
 * @property schoolNameResId School name string res id
 * @property authorNameResId Author name string res id
 * @property systemNameResId System name string res id
 * @property staticImportOptionsResId Import option string array res id (Useless for WebCourseProvider)
 * @property providerClass CourseProvider java class
 * @property parserClass CourseParser java class
 * @property providerParams Provider params
 * @property sortingBasis 用于列表排序（建议国内院校使用拼音，国际学校使用英文）
 * @constructor Create empty Course import config
 */
abstract class CourseImportConfig<P : Serializable, T1 : BaseCourseProvider<P>, T2 : ICourseParser>(
    @StringRes
    val schoolNameResId: Int,
    @StringRes
    val authorNameResId: Int,
    @StringRes
    val systemNameResId: Int,
    @ArrayRes
    val staticImportOptionsResId: Int? = null,
    // KClass不可序列化，此处只能使用Java的Class
    private val providerClass: Class<T1>,
    // KClass不可序列化，此处只能使用Java的Class
    private val parserClass: Class<T2>,
    private val providerParams: P? = null,
    val sortingBasis: String,
) : Serializable {

    fun newProvider(): T1 {
        val constructor = providerClass.constructors.first()
        return if (constructor.genericParameterTypes.isEmpty()) {
            constructor.newInstance()
        } else {
            constructor.newInstance(providerParams?.clone())
        }.cast()
    }

    fun newParser(): T2 = parserClass.newInstance()

    fun validateProviderType(clazz: KClass<out BaseCourseProvider<*>>) = clazz.java.isAssignableFrom(providerClass)

    fun validateParserType(clazz: KClass<out ICourseParser>) = clazz.java.isAssignableFrom(parserClass)
}