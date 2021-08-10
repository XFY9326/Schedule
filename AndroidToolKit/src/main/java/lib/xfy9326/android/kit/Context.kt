@file:Suppress("unused", "NOTHING_TO_INLINE")

package lib.xfy9326.android.kit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lib.xfy9326.kit.tryCast
import kotlin.system.exitProcess

inline fun Context.showToast(@StringRes resId: Int, vararg params: Any, showLong: Boolean = false) = showToast(getString(resId, *params), showLong)

inline fun Context.showToast(text: String, showLong: Boolean = false) = Toast.makeText(this, text, if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

inline fun showGlobalToast(@StringRes resId: Int, vararg params: Any, showLong: Boolean = false) = ApplicationInstance.showToast(resId, params, showLong)

inline fun showGlobalToast(text: String, showLong: Boolean = false) = ApplicationInstance.showToast(text, showLong)

inline fun Fragment.showToast(@StringRes resId: Int, vararg params: Any, showLong: Boolean = false) = requireContext().showToast(resId, params, showLong)

inline fun Fragment.showToast(text: String, showLong: Boolean = false) = requireContext().showToast(text, showLong)

inline fun <reified A : Activity> Context.startActivity(intentBlock: Intent.() -> Unit = {}) {
    startActivity(Intent(this, A::class.java).apply(intentBlock))
}

fun Context.relaunchApp(intentBlock: Intent.() -> Unit = {}) {
    startActivity(packageManager.getLaunchIntentForPackage(packageName)!!.apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intentBlock.invoke(this)
    })
    exitProcess(0)
}

inline fun Context.getColorCompat(@ColorRes id: Int) = ContextCompat.getColor(this, id)

inline fun Context.getDrawableCompat(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

inline fun Context.getStringArray(@ArrayRes id: Int): Array<String> = resources.getStringArray(id)

inline fun Context.getIntArray(@ArrayRes id: Int): IntArray = resources.getIntArray(id)

fun Context.isUsingNightMode(mode: Int = resources.configuration.uiMode) =
    mode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Context.hideKeyboard(windowToken: IBinder) {
    getSystemService<InputMethodManager>()?.apply {
        if (isActive) hideSoftInputFromWindow(windowToken, 0)
    }
}

@SuppressLint("QueryPermissionsNeeded")
fun Context.tryStartActivity(intent: Intent, options: Bundle? = null, onFailed: () -> Unit = {}): Boolean {
    // 由于兼容性问题，部分系统无法查询到Intent是否可以被处理
    val queryActivity = intent.resolveActivity(packageManager) != null
    if (queryActivity) {
        ContextCompat.startActivity(this, intent, options)
        return true
    } else {
        runCatching {
            ContextCompat.startActivity(this, intent, options)
            return true
        }.onFailure {
            onFailed()
        }
    }
    return false
}

@ColorInt
fun Context.getBackgroundColor(@ColorInt default: Int): Int {
    theme.obtainStyledAttributes(IntArray(1) {
        android.R.attr.colorBackground
    }).let { array ->
        return array.getColor(0, default).also {
            array.recycle()
        }
    }
}

@Px
fun Context.getActionBarDefaultHeight(): Int {
    theme.obtainStyledAttributes(IntArray(1) {
        android.R.attr.actionBarSize
    }).let { array ->
        return array.getDimensionPixelSize(0, 0).also {
            array.recycle()
        }
    }
}

fun <T> Fragment.requireOwner() = (parentFragment ?: requireActivity()).tryCast<T>()

fun Activity.getRealScreenSize(): Pair<Int, Int> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val size = Point()
        display?.getRealSize(size)
        Pair(size.x, size.y)
    } else {
        val size = Point()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealSize(size)
        Pair(size.x, size.y)
    }
}

fun BroadcastReceiver.goAsync(
    coroutineScope: CoroutineScope = ApplicationScope,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend () -> Unit,
) {
    val result = goAsync()
    coroutineScope.launch(dispatcher) {
        try {
            block()
        } finally {
            result.finish()
        }
    }
}