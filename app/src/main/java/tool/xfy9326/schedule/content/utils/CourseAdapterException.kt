@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import android.content.Context
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
    enum class ErrorType {
        IMPORT_SELECT_OPTION_ERROR,
        IMPORT_OPTION_GET_ERROR,
        USER_ID_ERROR,
        USER_PASSWORD_ERROR,
        USER_ID_OR_PASSWORD_ERROR,
        LOGIN_SERVER_ERROR,
        ACCOUNT_ERROR,
        CONNECTION_ERROR,
        UNKNOWN_ERROR,
        PARSER_ERROR,
        CAPTCHA_DOWNLOAD_ERROR,
        INIT_ERROR,
        CLOSE_ERROR,
        MAX_COURSE_NUM_ERROR,
        INCOMPLETE_COURSE_INFO_ERROR,
        CUSTOM_ERROR;

        /**
         * Throw exception
         *
         * @param cause Exception cause
         */
        fun report(cause: Throwable? = null): Nothing = throw make(cause)

        /**
         * Throw exception
         *
         * @param msg Exception msg
         * @param cause Exception cause
         */
        fun report(msg: String, cause: Throwable? = null): Nothing = throw make(msg, cause)


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
    }

    fun getText(context: Context) =
        if (errorType == ErrorType.CUSTOM_ERROR) {
            message.orEmpty()
        } else {
            context.getString(
                when (errorType) {
                    ErrorType.IMPORT_SELECT_OPTION_ERROR -> R.string.adapter_exception_import_option_error
                    ErrorType.IMPORT_OPTION_GET_ERROR -> R.string.adapter_exception_import_option_error
                    ErrorType.USER_ID_ERROR -> R.string.adapter_exception_login_user_id_error
                    ErrorType.USER_PASSWORD_ERROR -> R.string.adapter_exception_login_user_password_error
                    ErrorType.USER_ID_OR_PASSWORD_ERROR -> R.string.adapter_exception_login_user_id_or_password_error
                    ErrorType.LOGIN_SERVER_ERROR -> R.string.adapter_exception_login_server_error
                    ErrorType.ACCOUNT_ERROR -> R.string.adapter_exception_login_account_error
                    ErrorType.CONNECTION_ERROR -> R.string.adapter_exception_login_connection_error
                    ErrorType.UNKNOWN_ERROR -> R.string.adapter_exception_unknown_error
                    ErrorType.PARSER_ERROR -> R.string.adapter_exception_parse_error
                    ErrorType.CAPTCHA_DOWNLOAD_ERROR -> R.string.adapter_exception_captcha_download_error
                    ErrorType.INIT_ERROR -> R.string.adapter_exception_init_error
                    ErrorType.CLOSE_ERROR -> R.string.adapter_exception_close_error
                    ErrorType.MAX_COURSE_NUM_ERROR -> R.string.max_course_num_error
                    ErrorType.INCOMPLETE_COURSE_INFO_ERROR -> R.string.adapter_exception_incomplete_course_info
                    ErrorType.CUSTOM_ERROR -> error("Invalid course adapter error type!")
                }
            )
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