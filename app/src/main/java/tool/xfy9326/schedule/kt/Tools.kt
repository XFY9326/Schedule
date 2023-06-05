package tool.xfy9326.schedule.kt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import com.google.android.material.snackbar.Snackbar
import io.github.xfy9326.atools.core.relaunchApplication
import io.github.xfy9326.atools.core.tryStartActivity
import io.github.xfy9326.atools.ui.getColorCompat
import io.github.xfy9326.atools.ui.setLightSystemBar
import io.github.xfy9326.atools.ui.showToast
import kotlinx.coroutines.flow.Flow
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.SystemBarAppearance
import tool.xfy9326.schedule.ui.activity.module.ScheduleLaunchModule
import tool.xfy9326.schedule.utils.getBackgroundColor
import tool.xfy9326.schedule.utils.isUsingNightMode

const val PROJECT_ID = BuildConfig.PROJECT_NAME

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

fun Window.setSystemBarAppearance(systemBarAppearance: SystemBarAppearance) {
    when (systemBarAppearance) {
        SystemBarAppearance.FOLLOW_THEME -> setLightSystemBar(!decorView.context.isUsingNightMode())
        SystemBarAppearance.USE_LIGHT -> setLightSystemBar(true)
        SystemBarAppearance.USE_DARK -> setLightSystemBar(false)
    }
}

@ColorInt
fun Context.getDefaultBackgroundColor() = getBackgroundColor(getColorCompat(R.color.default_background))

fun CoordinatorLayout.showSnackBar(@StringRes strId: Int, vararg params: Any, showLong: Boolean = false) =
    showSnackBar(context.getString(strId, *params), showLong)

fun CoordinatorLayout.showSnackBar(str: String, showLong: Boolean = false) =
    Snackbar.make(this, str, if (showLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).show()

fun <T> Flow<T>.asDistinctLiveData(): LiveData<T> = asLiveData().distinctUntilChanged()