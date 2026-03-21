@file:Suppress("unused")

package tool.xfy9326.schedule.tools.ui

import android.content.Context
import androidx.annotation.StringRes
import tool.xfy9326.schedule.tools.core.AppContext
import tool.xfy9326.schedule.tools.core.showToast

fun Context.showToast(@StringRes resId: Int, vararg params: Any, lengthLong: Boolean = false): Unit =
    showToast(getString(resId, *params), lengthLong)

fun showGlobalToast(text: String, lengthLong: Boolean = false): Unit =
    AppContext.showToast(text, lengthLong)

fun showGlobalToast(@StringRes resId: Int, vararg params: Any, lengthLong: Boolean = false): Unit =
    AppContext.showToast(resId, params, lengthLong)
