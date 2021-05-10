@file:Suppress("unused", "NOTHING_TO_INLINE")

package tool.xfy9326.schedule.kt

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.ui.activity.SplashActivity
import kotlin.system.exitProcess

inline fun showGlobalToast(@StringRes resId: Int, vararg params: Any, showLong: Boolean = false) =
    Toast.makeText(App.instance, App.instance.getString(resId, *params), if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

inline fun showGlobalToast(text: String, showLong: Boolean = false) =
    Toast.makeText(App.instance, text, if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

inline fun Context.showToast(@StringRes resId: Int, vararg params: Any, showLong: Boolean = false) =
    Toast.makeText(this, getString(resId, *params), if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

inline fun Fragment.showToast(@StringRes resId: Int, vararg params: Any, showLong: Boolean = false) =
    Toast.makeText(requireContext(), getString(resId, *params), if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

inline fun Fragment.showToast(text: String, showLong: Boolean = false) =
    Toast.makeText(requireContext(), text, if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

inline fun <reified A : Activity> Context.startActivity(intentBlock: Intent.() -> Unit = {}) {
    startActivity(Intent(this, A::class.java).apply(intentBlock))
}

fun Context.crashRelaunch() {
    relaunchApp {
        putExtra(SplashActivity.INTENT_EXTRA_CRASH_RELAUNCH, true)
    }
}

fun Context.appErrorRelaunch(crashLogName: String?) {
    relaunchApp {
        putExtra(SplashActivity.INTENT_EXTRA_APP_ERROR, true)
        putExtra(SplashActivity.INTENT_EXTRA_APP_ERROR_CRASH_LOG, crashLogName)
    }
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

fun Context.tryStartActivity(intent: Intent, options: Bundle? = null, showToast: Boolean = true): Boolean {
    if (intent.resolveActivity(packageManager) != null) {
        ContextCompat.startActivity(this, intent, options)
        return true
    } else {
        if (showToast) showToast(R.string.application_launch_failed)
    }
    return false
}

@ColorInt
fun Context.getDefaultBackgroundColor(): Int {
    theme.obtainStyledAttributes(IntArray(1) {
        android.R.attr.colorBackground
    }).let { array ->
        return array.getColor(0, getColorCompat(R.color.default_background)).also {
            array.recycle()
        }
    }
}

fun <T> Fragment.requireOwner() = (parentFragment ?: requireActivity()).tryCast<T>()

fun BroadcastReceiver.goAsync(
    coroutineScope: CoroutineScope = GlobalScope,
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    block: suspend () -> Unit,
) {
    val result = goAsync()
    coroutineScope.launch(dispatcher) {
        try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            result.finish()
        }
    }
}