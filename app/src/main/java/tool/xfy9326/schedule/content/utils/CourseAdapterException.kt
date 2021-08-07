@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import android.content.Context
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

        fun CourseAdapterException.getText(context: Context) =
            if (type == Error.CUSTOM_ERROR) {
                message.orEmpty()
            } else {
                type.msgId?.let { context.getString(it) } ?: error("Unsupported error type!")
            }

        val Error.strictModeOnly
            get() = this == Error.SCHEDULE_COURSE_IMPORT_EMPTY || this == Error.FAILED_TO_IMPORT_SOME_COURSE

        private val Error.msgId: Int?
            get() = when (this) {
                Error.IMPORT_SELECT_OPTION_ERROR -> R.string.adapter_exception_import_option_error
                Error.IMPORT_OPTION_GET_ERROR -> R.string.adapter_exception_import_option_get_error
                Error.PARSE_PAGE_ERROR -> R.string.adapter_exception_parse_page_error
                Error.USER_ID_ERROR -> R.string.adapter_exception_login_user_id_error
                Error.USER_PASSWORD_ERROR -> R.string.adapter_exception_login_user_password_error
                Error.USER_ID_OR_PASSWORD_ERROR -> R.string.adapter_exception_login_user_id_or_password_error
                Error.LOGIN_SERVER_ERROR -> R.string.adapter_exception_login_server_error
                Error.ACCOUNT_ERROR -> R.string.adapter_exception_login_account_error
                Error.INCOMPLETE_COURSE_INFO_ERROR -> R.string.adapter_exception_incomplete_course_info
                Error.UNKNOWN_ERROR -> R.string.adapter_exception_unknown_error
                Error.CAPTCHA_CODE_ERROR -> R.string.adapter_exception_captcha_code_error

                Error.CONNECTION_ERROR -> R.string.adapter_exception_login_connection_error
                Error.PARSER_ERROR -> R.string.adapter_exception_parse_error
                Error.CAPTCHA_DOWNLOAD_ERROR -> R.string.adapter_exception_captcha_download_error

                Error.INIT_ERROR -> R.string.adapter_exception_init_error
                Error.CLOSE_ERROR -> R.string.adapter_exception_close_error

                Error.MAX_COURSE_NUM_ERROR -> R.string.adapter_exception_max_course_num_error
                Error.SCHEDULE_TIMES_ERROR -> R.string.adapter_exception_schedule_time_error
                Error.SCHEDULE_COURSE_IMPORT_EMPTY -> R.string.adapter_exception_schedule_course_empty
                Error.FAILED_TO_IMPORT_SOME_COURSE -> R.string.adapter_exception_failed_to_import_some_course

                Error.JS_HANDLE_ERROR -> R.string.adapter_exception_js_handle_error
                Error.CUSTOM_ERROR -> null
            }
    }

    enum class Error {
        IMPORT_SELECT_OPTION_ERROR,
        IMPORT_OPTION_GET_ERROR,
        PARSE_PAGE_ERROR,
        USER_ID_ERROR,
        USER_PASSWORD_ERROR,
        USER_ID_OR_PASSWORD_ERROR,
        LOGIN_SERVER_ERROR,
        ACCOUNT_ERROR,
        INCOMPLETE_COURSE_INFO_ERROR,
        UNKNOWN_ERROR,
        CAPTCHA_CODE_ERROR,

        // 课程导入时会自动判断该错误
        CONNECTION_ERROR,
        PARSER_ERROR,
        CAPTCHA_DOWNLOAD_ERROR,

        INIT_ERROR,
        CLOSE_ERROR,

        MAX_COURSE_NUM_ERROR,
        SCHEDULE_TIMES_ERROR,
        SCHEDULE_COURSE_IMPORT_EMPTY,
        FAILED_TO_IMPORT_SOME_COURSE,

        // 只给JSConfigException使用
        JS_HANDLE_ERROR,

        // 如果可以添加固定的报错内容，就不要使用该报错类型
        CUSTOM_ERROR;
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