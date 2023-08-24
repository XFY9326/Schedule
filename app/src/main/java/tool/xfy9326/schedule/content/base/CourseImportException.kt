@file:Suppress("unused")

package tool.xfy9326.schedule.content.base

import android.content.Context
import tool.xfy9326.schedule.utils.getDeepStackTraceString

abstract class CourseImportException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)

    abstract fun getText(context: Context): String

    open fun getDetailLog(): String = getDeepStackTraceString()
}