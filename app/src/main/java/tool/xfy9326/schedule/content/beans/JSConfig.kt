package tool.xfy9326.schedule.content.beans

import android.os.Parcelable
import android.webkit.URLUtil
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
    val initPageUrl: String,
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
        if (providerJSUrl.isBlank() || !URLUtil.isValidUrl(providerJSUrl)) JSConfigException.Error.PROVIDER_URL_ERROR.report()
        if (parserJSUrl.isBlank() || !URLUtil.isValidUrl(parserJSUrl)) JSConfigException.Error.PARSER_URL_ERROR.report()
        if (initPageUrl.isBlank() || !URLUtil.isValidUrl(initPageUrl)) JSConfigException.Error.INIT_PAGE_URL_ERROR.report()
        for (dependenciesJSUrl in dependenciesJSUrls) {
            if (dependenciesJSUrl.isBlank() || !URLUtil.isValidUrl(dependenciesJSUrl)) JSConfigException.Error.DEPENDENCY_URL_ERROR.report()
        }
    }

    fun getJSParams() = JSParams(uuid, jsType, initPageUrl)
}