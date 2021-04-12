package tool.xfy9326.schedule.content.beans

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import tool.xfy9326.schedule.content.base.ICourseImportConfig
import java.util.*

@Serializable
data class JSConfig(
    val uuid: String,
    val version: Int = VERSION,
    override val schoolName: String,
    override val authorName: String,
    override val systemName: String,
    val jsType: String = TYPE_PURE_SCHEDULE,
    val dependenciesJSUrls: List<String> = emptyList(),
    val providerJSUrl: String,
    val parserJSUrl: String,
    val updateUrl: String? = null,
    val sortingBasis: String,
) : ICourseImportConfig {
    companion object {
        private const val VERSION = 1

        const val TYPE_AI_SCHEDULE = "AiSchedule"
        const val TYPE_PURE_SCHEDULE = "PureSchedule"

        private fun String.isUUID() =
            try {
                UUID.fromString(this).toString()
                true
            } catch (e: Exception) {
                false
            }
    }

    @Transient
    override val lowerCaseSortingBasis = sortingBasis.toLowerCase(Locale.getDefault())

    init {
        require(uuid.isUUID()) { "JSCourseImportConfig UUID error! UUID: $uuid" }
        require(schoolName.isNotBlank()) { "JSCourseImportConfig school name empty!" }
        require(authorName.isNotBlank()) { "JSCourseImportConfig author name empty!" }
        require(systemName.isNotBlank()) { "JSCourseImportConfig system name empty!" }
        require(jsType == TYPE_AI_SCHEDULE || jsType == TYPE_PURE_SCHEDULE) { "JSCourseImportConfig type error! Type: $jsType" }
        require(providerJSUrl.isNotBlank()) { "JSCourseImportConfig provider js empty!" }
        require(parserJSUrl.isNotBlank()) { "JSCourseImportConfig parser js empty!" }
    }

    fun getJSParams() = JSParams(uuid, jsType)
}