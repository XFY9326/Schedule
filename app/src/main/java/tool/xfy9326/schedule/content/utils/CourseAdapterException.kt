@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import android.content.Context
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.base.CourseImportException

class CourseAdapterException : CourseImportException {
    val type: Error

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun Error.report(cause: Throwable? = null, msg: String? = null): Nothing = throw make(cause, msg)

        fun Error.make(cause: Throwable? = null, msg: String? = null) =
            if (cause == null) {
                CourseAdapterException(this, msg)
            } else {
                CourseAdapterException(this, cause, msg)
            }

        @Suppress("NOTHING_TO_INLINE")
        inline fun reportCustomError(msg: String, cause: Throwable? = null): Nothing = throw makeCustomError(msg, cause)

        fun makeCustomError(msg: String, cause: Throwable? = null) =
            if (cause == null) {
                CourseAdapterException(msg)
            } else {
                CourseAdapterException(msg, cause)
            }

        val Error.strictModeOnly
            get() = this == Error.SCHEDULE_COURSE_IMPORT_EMPTY || this == Error.FAILED_TO_IMPORT_SOME_COURSE || this == Error.CONTENT_PARSE_ERROR

        private val Error.msgId: Int?
            get() = when (this) {
                Error.IMPORT_SELECT_OPTION_ERROR -> R.string.adapter_exception_import_option_error
                Error.PARSE_PAGE_ERROR -> R.string.adapter_exception_parse_page_error
                Error.USER_ID_ERROR -> R.string.adapter_exception_login_user_id_error
                Error.USER_PASSWORD_ERROR -> R.string.adapter_exception_login_user_password_error
                Error.USER_ID_OR_PASSWORD_ERROR -> R.string.adapter_exception_login_user_id_or_password_error
                Error.LOGIN_SERVER_ERROR -> R.string.adapter_exception_login_server_error
                Error.ACCOUNT_ERROR -> R.string.adapter_exception_login_account_error
                Error.INCOMPLETE_COURSE_INFO_ERROR -> R.string.adapter_exception_incomplete_course_info
                Error.CONTENT_PARSE_ERROR -> R.string.adapter_exception_content_parse_error
                Error.CAPTCHA_CODE_ERROR -> R.string.adapter_exception_captcha_code_error
                Error.JSON_PARSE_ERROR -> R.string.adapter_exception_json_parse_error

                Error.CUSTOM_ERROR -> null

                Error.CONNECTION_ERROR -> R.string.adapter_exception_login_connection_error
                Error.CAPTCHA_DOWNLOAD_ERROR -> R.string.adapter_exception_captcha_download_error
                Error.IMPORT_OPTION_GET_ERROR -> R.string.adapter_exception_import_option_get_error
                Error.UNKNOWN_ERROR -> R.string.adapter_exception_unknown_error

                Error.INIT_ERROR -> R.string.adapter_exception_init_error
                Error.CLOSE_ERROR -> R.string.adapter_exception_close_error

                Error.MAX_COURSE_NUM_ERROR -> R.string.adapter_exception_max_course_num_error
                Error.SCHEDULE_TIMES_ERROR -> R.string.adapter_exception_schedule_time_error
                Error.SCHEDULE_COURSE_IMPORT_EMPTY -> R.string.adapter_exception_schedule_course_empty
                Error.FAILED_TO_IMPORT_SOME_COURSE -> R.string.adapter_exception_failed_to_import_some_course

                Error.JS_HANDLE_ERROR -> R.string.adapter_exception_js_handle_error
                Error.JS_RESULT_PARSE_ERROR -> R.string.adapter_exception_js_result_parse_error
            }
    }

    // 除非特殊说明，否则均为不可以忽略的错误
    enum class Error {
        IMPORT_SELECT_OPTION_ERROR, // 主要用于未知的导入选项导致的错误
        PARSE_PAGE_ERROR, // 主要用于传入的页面无法解析导致的错误
        USER_ID_ERROR, // 主要用于用户名错误导致的无法登录
        USER_PASSWORD_ERROR, // 主要用于密码错误导致的无法登录
        USER_ID_OR_PASSWORD_ERROR,  // 主要用于用户名或密码错误导致的无法登录
        LOGIN_SERVER_ERROR, // 主要用于登录服务器错误
        ACCOUNT_ERROR, // 主要用于登录账号错误
        INCOMPLETE_COURSE_INFO_ERROR, // 主要用于课程信息不完整导致的错误
        CONTENT_PARSE_ERROR, // 主要用于内容解析导致的错误（严格模式下启用，即可以忽略或跳过的错误）
        CAPTCHA_CODE_ERROR, // 主要用于验证码错误
        JSON_PARSE_ERROR,  // 主要用于JSON解析错误

        // 如果可以添加固定的报错内容，就不要使用该报错类型
        // 此类型上报的错误内容会直接显示到用户界面的提示处
        // 使用CourseAdapterException.reportCustomError与CourseAdapterException.makeCustomError设定该类错误的具体内容
        CUSTOM_ERROR,

        /* ----- 以下错误为课程导入时会自动判断的错误，不建议手动调用，手动调用可能导致重复报错 ----- */

        IMPORT_OPTION_GET_ERROR,
        CONNECTION_ERROR,
        CAPTCHA_DOWNLOAD_ERROR,
        UNKNOWN_ERROR,

        INIT_ERROR,
        CLOSE_ERROR,

        MAX_COURSE_NUM_ERROR,
        SCHEDULE_TIMES_ERROR,

        // 严格模式下启用
        SCHEDULE_COURSE_IMPORT_EMPTY,

        // 严格模式下启用
        // 仅用于出现非CourseAdapterException的错误，即意外错误时
        FAILED_TO_IMPORT_SOME_COURSE,

        // 只给JSConfigException使用
        JS_HANDLE_ERROR,
        JS_RESULT_PARSE_ERROR;
    }

    private constructor(msg: String) : super(msg) {
        this.type = Error.CUSTOM_ERROR
    }

    private constructor(msg: String, cause: Throwable) : super(msg, cause) {
        this.type = Error.CUSTOM_ERROR
    }

    private constructor(type: Error, msg: String? = null) : super(msg ?: type.name) {
        this.type = type
    }

    private constructor(type: Error, cause: Throwable, msg: String? = null) : super(msg ?: type.name, cause) {
        this.type = type
    }

    override fun getText(context: Context) =
        if (type == Error.CUSTOM_ERROR) {
            message.orEmpty()
        } else {
            type.msgId?.let { context.getString(it) } ?: error("Unsupported error type!")
        }
}