@file:Suppress("unused", "NOTHING_TO_INLINE")

package tool.xfy9326.schedule.kt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.ui.activity.SplashActivity
import kotlin.system.exitProcess

inline fun showGlobalShortToast(@StringRes resId: Int, vararg params: Any) =
    Toast.makeText(App.instance, App.instance.getString(resId, *params), Toast.LENGTH_SHORT).show()

inline fun showGlobalLongToast(@StringRes resId: Int, vararg params: Any) =
    Toast.makeText(App.instance, App.instance.getString(resId, *params), Toast.LENGTH_LONG).show()

inline fun Context.showShortToast(@StringRes resId: Int, vararg params: Any) =
    Toast.makeText(this, getString(resId, *params), Toast.LENGTH_SHORT).show()

inline fun Context.showLongToast(@StringRes resId: Int, vararg params: Any) =
    Toast.makeText(this, getString(resId, *params), Toast.LENGTH_LONG).show()

inline fun Fragment.showShortToast(@StringRes resId: Int, vararg params: Any) =
    Toast.makeText(requireContext(), getString(resId, *params), Toast.LENGTH_SHORT).show()

inline fun Fragment.showLongToast(@StringRes resId: Int, vararg params: Any) =
    Toast.makeText(requireContext(), getString(resId, *params), Toast.LENGTH_LONG).show()

inline fun <reified A : Activity> Context.startActivity(intentBlock: Intent.() -> Unit = {}) {
    startActivity(Intent(this, A::class.java).apply(intentBlock))
}

fun Context.crashRelaunch() {
    relaunchApp(true) {
        putExtra(SplashActivity.INTENT_EXTRA_CRASH_RELAUNCH, true)
    }
}

fun Context.appErrorRelaunch(crashLogName: String?) {
    relaunchApp(true) {
        putExtra(SplashActivity.INTENT_EXTRA_APP_ERROR, true)
        putExtra(SplashActivity.INTENT_EXTRA_APP_ERROR_CRASH_LOG, crashLogName)
    }
}

fun Context.relaunchApp(killOldProcess: Boolean, intentBlock: Intent.() -> Unit = {}) {
    startActivity(packageManager.getLaunchIntentForPackage(packageName)!!.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intentBlock.invoke(this)
    })
    if (killOldProcess) exitProcess(0)
}

inline fun Context.getColorCompat(@ColorRes id: Int) = ContextCompat.getColor(this, id)

inline fun Context.getDrawableCompat(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

inline fun Context.getStringArray(@ArrayRes id: Int): Array<String> = resources.getStringArray(id)

inline fun Context.getIntArray(@ArrayRes id: Int): IntArray = resources.getIntArray(id)

fun Context.isUsingNightMode() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Context.hideKeyboard(windowToken: IBinder) {
    getSystemService<InputMethodManager>()?.apply {
        if (isActive) hideSoftInputFromWindow(windowToken, 0)
    }
}

fun Context.tryStartActivity(intent: Intent, options: Bundle? = null, showToast: Boolean = true): Boolean {
    if (intent.resolveActivity(packageManager) != null) {
        ContextCompat.startActivity(this, intent, options)
        return true
    } else if (showToast) {
        showShortToast(R.string.application_launch_failed)
    }
    return false
}