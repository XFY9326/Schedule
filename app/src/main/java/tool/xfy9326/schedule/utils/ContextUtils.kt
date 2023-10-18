package tool.xfy9326.schedule.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.res.use
import io.github.xfy9326.atools.core.relaunchApplication
import io.github.xfy9326.atools.core.tryStartActivity
import io.github.xfy9326.atools.ui.getColorCompat
import io.github.xfy9326.atools.ui.showToast
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.ui.activity.module.ScheduleLaunchModule


fun Context.isUsingNightMode(mode: Int = resources.configuration.uiMode) =
    mode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

@ColorInt
fun Context.getBackgroundColor(@ColorInt default: Int): Int {
    theme.obtainStyledAttributes(intArrayOf(android.R.attr.colorBackground)).use {
        return it.getColor(0, default)
    }
}

fun Context.crashRelaunch() {
    relaunchApplication(1) {
        putExtra(ScheduleLaunchModule.INTENT_EXTRA_CRASH_RELAUNCH, true)
    }
}

fun Context.appErrorRelaunch(crashLogPath: String) {
    relaunchApplication(1) {
        putExtra(ScheduleLaunchModule.INTENT_EXTRA_APP_ERROR, true)
        putExtra(ScheduleLaunchModule.INTENT_EXTRA_APP_ERROR_CRASH_LOG, crashLogPath)
    }
}

fun Context.tryStartActivity(intent: Intent, options: Bundle? = null, showToast: Boolean = true) =
    tryStartActivity(intent, options).also {
        if (!it && showToast) showToast(R.string.application_launch_failed)
    }

@ColorInt
fun Context.getDefaultBackgroundColor() = getBackgroundColor(getColorCompat(R.color.default_background))

@Px
fun Context.getActionBarSize(): Int =
    theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize)).use {
        it.getDimension(0, 0f).toInt()
    }
