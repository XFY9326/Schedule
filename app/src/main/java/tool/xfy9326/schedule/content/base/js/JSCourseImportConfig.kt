package tool.xfy9326.schedule.content.base.js

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.content.beans.CourseImportInstance
import java.util.*

@Parcelize
@Serializable
data class JSCourseImportConfig(
    val version: Int = VERSION,
    val schoolName: String,
    val authorName: String,
    val systemName: String,
    val type: String = TYPE_PURE_SCHEDULE,
    val dependencyJS: List<String> = emptyList(),
    val providerJS: String,
    val parserJS: String,
    val updateUrl: String? = null,
    val sortingBasis: String,
) : Parcelable {
    companion object {
        private const val VERSION = 1

        const val TYPE_AI_SCHEDULE = "AiSchedule"
        const val TYPE_PURE_SCHEDULE = "PureSchedule"
    }

    init {
        require(schoolName.isNotBlank()) { "JSCourseImportConfig school name empty!" }
        require(authorName.isNotBlank()) { "JSCourseImportConfig author name empty!" }
        require(systemName.isNotBlank()) { "JSCourseImportConfig system name empty!" }
        require(type == TYPE_AI_SCHEDULE || type == TYPE_PURE_SCHEDULE) { "JSCourseImportConfig type error!" }
        require(providerJS.isNotBlank()) { "JSCourseImportConfig provider js empty!" }
        require(parserJS.isNotBlank()) { "JSCourseImportConfig parser jd empty!" }
    }

    val lowerCaseSortingBasis
        get() = sortingBasis.toLowerCase(Locale.getDefault())

    fun getInstance(id: String) = CourseImportInstance(
        schoolName,
        authorName,
        systemName,
        provider = JSCourseProvider(id),
        parser = JSCourseParser(id)
    )
}