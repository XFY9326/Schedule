@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import android.content.Context
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.base.CourseImportException

class JSConfigException : CourseImportException {
    val type: Error

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun Error.report(cause: Throwable? = null): Unit = throw make(cause)

        fun Error.make(cause: Throwable? = null) =
            if (cause == null) {
                JSConfigException(this)
            } else {
                JSConfigException(this, cause)
            }

        private val Error.msgId
            get() = when (this) {
                Error.READ_FAILED -> R.string.js_config_read_failed
                Error.INVALID -> R.string.js_config_invalid
                Error.UUID_ERROR -> R.string.js_config_uuid_error
                Error.JS_TYPE_ERROR -> R.string.js_config_js_type_error
                Error.PROVIDER_URL_ERROR -> R.string.js_config_provider_url_error
                Error.PARSER_URL_ERROR -> R.string.js_config_parser_url_error
                Error.INIT_PAGE_URL_ERROR -> R.string.js_config_init_page_url_error
                Error.DEPENDENCY_URL_ERROR -> R.string.js_config_dependency_url_error

                Error.UNKNOWN_ERROR -> R.string.js_config_unknown_error
                Error.CONFIG_DELETE_ERROR -> R.string.js_config_config_delete_error
                Error.UPDATE_FAILED -> R.string.js_config_update_failed
                Error.PROVIDER_DOWNLOAD_ERROR -> R.string.js_config_provider_download_error
                Error.PARSER_DOWNLOAD_ERROR -> R.string.js_config_parser_download_error
                Error.DEPENDENCIES_DOWNLOAD_ERROR -> R.string.js_config_dependencies_download_error
                Error.PREPARE_ERROR -> R.string.js_config_prepare_error
                Error.INCOMPATIBLE_VERSION_ERROR -> R.string.js_config_incompatible_version_error
            }
    }

    enum class Error {
        READ_FAILED,
        INVALID,
        UUID_ERROR,
        JS_TYPE_ERROR,
        PROVIDER_URL_ERROR,
        PARSER_URL_ERROR,
        INIT_PAGE_URL_ERROR,
        DEPENDENCY_URL_ERROR,

        UNKNOWN_ERROR,
        CONFIG_DELETE_ERROR,
        UPDATE_FAILED,
        PROVIDER_DOWNLOAD_ERROR,
        PARSER_DOWNLOAD_ERROR,
        DEPENDENCIES_DOWNLOAD_ERROR,
        PREPARE_ERROR,
        INCOMPATIBLE_VERSION_ERROR
    }

    private constructor(type: Error) : super(type.name) {
        this.type = type
    }

    private constructor(type: Error, cause: Throwable) : super(type.name, cause) {
        this.type = type
    }

    override fun getText(context: Context) = context.getString(type.msgId)
}