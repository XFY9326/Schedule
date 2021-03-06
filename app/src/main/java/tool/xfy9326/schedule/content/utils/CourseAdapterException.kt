@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import android.content.Context
import androidx.annotation.StringRes
import tool.xfy9326.schedule.R

class CourseAdapterException : Exception {
    val type: Error

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun Error.report(cause: Throwable? = null): Nothing = throw make(cause)

        @Suppress("NOTHING_TO_INLINE")
        inline fun Error.report(msg: String, cause: Throwable? = null): Nothing = throw make(msg, cause)

        fun Error.make(cause: Throwable? = null) =
            if (cause == null) {
                CourseAdapterException(this)
            } else {
                CourseAdapterException(this, cause)
            }

        fun Error.make(msg: String, cause: Throwable? = null) =
            if (cause == null) {
                CourseAdapterException(msg)
            } else {
                CourseAdapterException(msg, cause)
            }

        fun Error.getText(context: Context) =
            if (msgId == null) {
                error("Unsupported error type!")
            } else {
                context.getString(msgId)
            }
    }

    enum class Error(@StringRes val msgId: Int?, val strictModeOnly: Boolean = false) {
        IMPORT_SELECT_OPTION_ERROR(R.string.adapter_exception_import_option_error),
        IMPORT_OPTION_GET_ERROR(R.string.adapter_exception_import_option_get_error),
        PARSE_PAGE_ERROR(R.string.adapter_exception_parse_page_error),
        USER_ID_ERROR(R.string.adapter_exception_login_user_id_error),
        USER_PASSWORD_ERROR(R.string.adapter_exception_login_user_password_error),
        USER_ID_OR_PASSWORD_ERROR(R.string.adapter_exception_login_user_id_or_password_error),
        LOGIN_SERVER_ERROR(R.string.adapter_exception_login_server_error),
        ACCOUNT_ERROR(R.string.adapter_exception_login_account_error),
        INCOMPLETE_COURSE_INFO_ERROR(R.string.adapter_exception_incomplete_course_info),
        UNKNOWN_ERROR(R.string.adapter_exception_unknown_error),
        CAPTCHA_CODE_ERROR(R.string.adapter_exception_captcha_code_error),

        // 课程导入时会自动判断该错误
        CONNECTION_ERROR(R.string.adapter_exception_login_connection_error),
        PARSER_ERROR(R.string.adapter_exception_parse_error),
        CAPTCHA_DOWNLOAD_ERROR(R.string.adapter_exception_captcha_download_error),

        INIT_ERROR(R.string.adapter_exception_init_error),
        CLOSE_ERROR(R.string.adapter_exception_close_error),

        MAX_COURSE_NUM_ERROR(R.string.adapter_exception_max_course_num_error),
        SCHEDULE_TIMES_ERROR(R.string.adapter_exception_schedule_time_error),
        SCHEDULE_COURSE_IMPORT_EMPTY(R.string.adapter_exception_schedule_course_empty, true),
        FAILED_TO_IMPORT_SOME_COURSE(R.string.adapter_exception_failed_to_import_some_course, true),

        // 只给JSConfigException使用
        JS_HANDLE_ERROR(R.string.adapter_exception_js_handle_error),

        // 如果可以添加固定的报错内容，就不要使用该报错类型
        CUSTOM_ERROR(null);
    }

    fun getText(context: Context) =
        if (type == Error.CUSTOM_ERROR) {
            message.orEmpty()
        } else {
            type.getText(context)
        }

    private constructor(msg: String) : super(msg) {
        this.type = Error.CUSTOM_ERROR
    }

    private constructor(msg: String, cause: Throwable) : super(msg, cause) {
        this.type = Error.CUSTOM_ERROR
    }

    private constructor(type: Error) : super(type.name) {
        this.type = type
    }

    private constructor(type: Error, cause: Throwable) : super(type.name, cause) {
        this.type = type
    }
}