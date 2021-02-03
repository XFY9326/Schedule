@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.base

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.github.promeg.pinyinhelper.Pinyin
import tool.xfy9326.schedule.io.GlobalIO
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * Course import config
 *
 * @param T1 CourseProvider
 * @param T2 CourseParser
 * @property schoolName School name string res id
 * @property authorName Author name string res id
 * @property systemName System name string res id
 * @property importOptions Import option string array res id (Useless for WebCourseProvider)
 * @property providerClass CourseProvider java class
 * @property parserClass CourseParser java class
 * @property providerParams Provider params
 * @constructor Create empty Course import config
 */
abstract class CourseImportConfig<T1 : BaseCourseProvider, T2 : ICourseParser>(
    @StringRes
    private val schoolName: Int,
    @StringRes
    private val authorName: Int,
    @StringRes
    private val systemName: Int,
    @ArrayRes
    private val importOptions: Int? = null,
    private val providerClass: Class<out T1>,
    private val parserClass: Class<out T2>,
    private val providerParams: Array<Any?> = emptyArray(),
) : Serializable {
    companion object {
        private fun getPinyin(str: String): String =
            StringBuilder().apply {
                for (c in str) {
                    if (c != ' ') append(Pinyin.toPinyin(c))
                }
            }.toString()
    }

    val schoolNameText by lazy {
        GlobalIO.resources.getString(schoolName)
    }
    val authorNameText by lazy {
        GlobalIO.resources.getString(authorName)
    }
    val systemNameText by lazy {
        GlobalIO.resources.getString(systemName)
    }

    val importOptionsArrayText: Array<String>? by lazy {
        importOptions?.let(GlobalIO.resources::getStringArray)
    }

    val schoolNamePinyin by lazy {
        getPinyin(schoolNameText)
    }
    val systemNamePinyin by lazy {
        getPinyin(systemNameText)
    }

    fun newProvider(): T1 = providerClass.newInstance().apply {
        initParams(providerParams.copyOf())
    }

    fun newParser(): T2 = parserClass.newInstance()

    fun <T : BaseCourseProvider> validateProviderType(clazz: KClass<T>) = clazz.java.isAssignableFrom(providerClass)

    fun <T : ICourseParser> validateParserType(clazz: KClass<T>) = clazz.java.isAssignableFrom(parserClass)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CourseImportConfig<*, *>) return false

        if (schoolName != other.schoolName) return false
        if (authorName != other.authorName) return false
        if (systemName != other.systemName) return false
        if (importOptions != other.importOptions) return false
        if (providerClass != other.providerClass) return false
        if (parserClass != other.parserClass) return false
        if (!providerParams.contentEquals(other.providerParams)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = schoolName
        result = 31 * result + authorName
        result = 31 * result + systemName
        result = 31 * result + (importOptions ?: 0)
        result = 31 * result + providerClass.hashCode()
        result = 31 * result + parserClass.hashCode()
        result = 31 * result + providerParams.contentHashCode()
        return result
    }
}