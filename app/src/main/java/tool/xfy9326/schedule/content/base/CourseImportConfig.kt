@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.base

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.github.promeg.pinyinhelper.Pinyin
import java.io.Serializable
import kotlin.reflect.KClass

abstract class CourseImportConfig<T1 : ICourseProvider, T2 : ICourseParser>(
    @StringRes
    val schoolName: Int,
    @StringRes
    val authorName: Int,
    @StringRes
    val systemName: Int,
    @ArrayRes
    val importOptions: Int? = null,
    val providerClass: Class<T1>,
    val parserClass: Class<T2>,
) : Serializable {
    companion object {
        private fun getPinyin(str: String): String =
            StringBuilder().apply {
                for (c in str) {
                    if (c != ' ') append(Pinyin.toPinyin(c))
                }
            }.toString()
    }

    private var schoolNameTextCache: String? = null
    private var schoolNameWordsCache: String? = null
    private var authorNameTextCache: String? = null
    private var systemNameTextCache: String? = null
    private var systemNameWordsCache: String? = null

    fun newProvider(): T1 = providerClass.newInstance()

    fun newParser(): T2 = parserClass.newInstance()

    fun <T : ICourseProvider> validateProviderType(clazz: KClass<T>) = clazz.java.isAssignableFrom(providerClass)

    fun <T : ICourseParser> validateParserType(clazz: KClass<T>) = clazz.java.isAssignableFrom(parserClass)

    fun getSchoolNameText(context: Context) =
        schoolNameTextCache ?: context.getString(schoolName).also {
            schoolNameTextCache = it
        }

    fun getAuthorNameText(context: Context) =
        authorNameTextCache ?: context.getString(authorName).also {
            authorNameTextCache = it
        }

    fun getSystemNameText(context: Context) =
        systemNameTextCache ?: context.getString(systemName).also {
            systemNameTextCache = it
        }

    fun getSchoolNameWords(context: Context) =
        schoolNameWordsCache ?: getPinyin(getSchoolNameText(context)).also {
            schoolNameWordsCache = it
        }

    fun getSystemNameWords(context: Context) =
        systemNameWordsCache ?: getPinyin(getSystemNameText(context)).also {
            systemNameWordsCache = it
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CourseImportConfig<*, *>) return false

        if (schoolName != other.schoolName) return false
        if (authorName != other.authorName) return false
        if (systemName != other.systemName) return false
        if (importOptions != other.importOptions) return false
        if (providerClass != other.providerClass) return false
        if (parserClass != other.parserClass) return false

        return true
    }

    override fun hashCode(): Int {
        var result = schoolName
        result = 31 * result + authorName
        result = 31 * result + systemName
        result = 31 * result + (importOptions ?: 0)
        result = 31 * result + providerClass.hashCode()
        result = 31 * result + parserClass.hashCode()
        return result
    }
}