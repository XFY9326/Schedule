@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import android.content.Context
import androidx.annotation.StringRes
import tool.xfy9326.schedule.R

/**
 * Course adapter exception
 *
 * @constructor Create empty Course adapter exception
 */
class CourseAdapterException : Exception {
    private val errorType: ErrorType

    /**
     * Error type
     *
     * @constructor Create empty Error type
     */
    enum class ErrorType(@StringRes private val msgId: Int?) {
        IMPORT_SELECT_OPTION_ERROR(R.string.adapter_exception_import_option_error),
        IMPORT_OPTION_GET_ERROR(R.string.adapter_exception_import_option_get_error),
        PARSE_PAGE_ERROR(R.string.adapter_exception_parse_page_error),
        PARSE_INFO_NOT_ENOUGH_ERROR(R.string.adapter_exception_parse_info_not_enough_error),
        USER_ID_ERROR(R.string.adapter_exception_login_user_id_error),
        USER_PASSWORD_ERROR(R.string.adapter_exception_login_user_password_error),
        USER_ID_OR_PASSWORD_ERROR(R.string.adapter_exception_login_user_id_or_password_error),
        LOGIN_SERVER_ERROR(R.string.adapter_exception_login_server_error),
        ACCOUNT_ERROR(R.string.adapter_exception_login_account_error),
        CONNECTION_ERROR(R.string.adapter_exception_login_connection_error),
        UNKNOWN_ERROR(R.string.adapter_exception_unknown_error),
        PARSER_ERROR(R.string.adapter_exception_parse_error),
        CAPTCHA_DOWNLOAD_ERROR(R.string.adapter_exception_captcha_download_error),
        INIT_ERROR(R.string.adapter_exception_init_error),
        CLOSE_ERROR(R.string.adapter_exception_close_error),
        MAX_COURSE_NUM_ERROR(R.string.adapter_exception_max_course_num_error),
        INCOMPLETE_COURSE_INFO_ERROR(R.string.adapter_exception_incomplete_course_info),
        SCHEDULE_TIMES_ERROR(R.string.adapter_exception_schedule_time_error),
        CUSTOM_ERROR(null);

        /**
         * Throw exception
         *
         * @param cause Exception cause
         */
        @Suppress("NOTHING_TO_INLINE")
        inline fun report(cause: Throwable? = null): Nothing = throw make(cause)

        /**
         * Throw exception
         *
         * @param msg Exception msg
         * @param cause Exception cause
         */
        @Suppress("NOTHING_TO_INLINE")
        inline fun report(msg: String, cause: Throwable? = null): Nothing = throw make(msg, cause)


        /**
         * Make exception
         *
         * @param cause Exception cause
         */
        fun make(cause: Throwable? = null) =
            if (cause == null) {
                CourseAdapterException(this)
            } else {
                CourseAdapterException(this, cause)
            }

        /**
         * Make exception
         *
         * @param msg Exception msg
         * @param cause Exception cause
         */
        fun make(msg: String, cause: Throwable? = null) =
            if (cause == null) {
                CourseAdapterException(msg)
            } else {
                CourseAdapterException(msg, cause)
            }

        fun getText(context: Context) =
            if (msgId == null) {
                error("Unsupported error type!")
            } else {
                context.getString(msgId)
            }
    }

    fun getText(context: Context) =
        if (errorType == ErrorType.CUSTOM_ERROR) {
            message.orEmpty()
        } else {
            errorType.getText(context)
        }

    private constructor(msg: String) : super(msg) {
        this.errorType = ErrorType.CUSTOM_ERROR
    }

    private constructor(msg: String, cause: Throwable) : super(msg, cause) {
        this.errorType = ErrorType.CUSTOM_ERROR
    }

    private constructor(type: ErrorType) : super(type.name) {
        this.errorType = type
    }

    private constructor(type: ErrorType, cause: Throwable) : super(type.name, cause) {
        this.errorType = type
    }
}