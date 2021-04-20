@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import android.content.Context
import androidx.annotation.StringRes
import tool.xfy9326.schedule.R

class JSConfigException : Exception {
    val type: Error

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun Error.report(cause: Throwable? = null): Nothing = throw make(cause)

        fun Error.make(cause: Throwable? = null) =
            if (cause == null) {
                JSConfigException(this)
            } else {
                JSConfigException(this, cause)
            }

        fun Error.getText(context: Context) = context.getString(msgId)
    }

    enum class Error(@StringRes val msgId: Int) {
        READ_FAILED(R.string.js_config_read_failed),
        INVALID(R.string.js_config_invalid),
        UUID_ERROR(R.string.js_config_uuid_error),
        JS_TYPE_ERROR(R.string.js_config_js_type_error),
        PROVIDER_URL_EMPTY(R.string.js_config_provider_url_empty),
        PARSER_URL_EMPTY(R.string.js_config_parser_url_empty),

        UNKNOWN_ERROR(R.string.js_config_unknown_error),
        CONFIG_DELETE_ERROR(R.string.js_config_config_delete_error),
        UPDATE_FAILED(R.string.js_config_update_failed),
        PROVIDER_DOWNLOAD_ERROR(R.string.js_config_provider_download_error),
        PARSER_DOWNLOAD_ERROR(R.string.js_config_parser_download_error),
        DEPENDENCIES_DOWNLOAD_ERROR(R.string.js_config_dependencies_download_error),
        PREPARE_ERROR(R.string.js_config_prepare_error)
    }

    fun getText(context: Context) = type.getText(context)

    private constructor(type: Error) : super(type.name) {
        this.type = type
    }

    private constructor(type: Error, cause: Throwable) : super(type.name, cause) {
        this.type = type
    }
}