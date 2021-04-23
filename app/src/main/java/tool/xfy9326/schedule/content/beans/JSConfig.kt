package tool.xfy9326.schedule.content.beans

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import tool.xfy9326.schedule.content.base.ICourseImportConfig
import tool.xfy9326.schedule.content.utils.JSConfigException
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.report
import java.util.*

@Parcelize
@Serializable
data class JSConfig(
    val uuid: String,
    val config: Int = CONFIG,
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
) : Parcelable, ICourseImportConfig {
    companion object {
        private const val CONFIG = 1
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
    @IgnoredOnParcel
    override val lowerCaseSortingBasis = sortingBasis.toLowerCase(Locale.getDefault())

    init {
        if (!uuid.isUUID()) JSConfigException.Error.UUID_ERROR.report()
        if (jsType != TYPE_AI_SCHEDULE && jsType != TYPE_PURE_SCHEDULE) JSConfigException.Error.JS_TYPE_ERROR.report()
        if (providerJSUrl.isBlank()) JSConfigException.Error.PROVIDER_URL_EMPTY.report()
        if (parserJSUrl.isBlank()) JSConfigException.Error.PARSER_URL_EMPTY.report()
    }

    fun getJSParams() = JSParams(uuid, jsType)
}