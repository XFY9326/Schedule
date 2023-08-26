package tool.xfy9326.schedule.kt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

fun View.consumeSystemBarInsets(top: Boolean = false, bottom: Boolean = false, margin: Boolean = false, keepOld: Boolean = false) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars().or(WindowInsetsCompat.Type.displayCutout()))
        if (margin) {
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(
                    left = (if (keepOld) v.marginLeft else 0) + systemInsets.left,
                    right = (if (keepOld) v.marginRight else 0) + systemInsets.right,
                    top = if (top) (if (keepOld) v.marginTop else 0) + systemInsets.top else v.marginTop,
                    bottom = if (bottom) (if (keepOld) v.marginBottom else 0) + systemInsets.bottom else v.marginBottom
                )
            }
        } else {
            v.updatePadding(
                left = (if (keepOld) v.paddingLeft else 0) + systemInsets.left,
                right = (if (keepOld) v.paddingRight else 0) + systemInsets.right,
                top = if (top) (if (keepOld) v.paddingTop else 0) + systemInsets.top else v.paddingTop,
                bottom = if (bottom) (if (keepOld) v.paddingBottom else 0) + systemInsets.bottom else v.paddingBottom
            )
        }
        WindowInsetsCompat.CONSUMED
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun Window.pixelCopy(config: Bitmap.Config) = suspendCoroutine {
    val bitmap = Bitmap.createBitmap(decorView.width, decorView.height, config)
    PixelCopy.request(this, bitmap, { status ->
        if (status == PixelCopy.SUCCESS) {
            it.resume(bitmap)
        } else {
            it.resumeWithException(IllegalStateException("Pixel copy failed!"))
        }
    }, Handler(Looper.getMainLooper()))
}

suspend fun Window.drawToBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            return pixelCopy(config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return decorView.drawToBitmap()
}

@ColorInt
fun Context.getDefaultBackgroundColor() = getBackgroundColor(getColorCompat(R.color.default_background))

fun CoordinatorLayout.showSnackBar(@StringRes strId: Int, vararg params: Any, showLong: Boolean = false) =
    showSnackBar(context.getString(strId, *params), showLong)

fun CoordinatorLayout.showSnackBar(str: String, showLong: Boolean = false) =
    Snackbar.make(this, str, if (showLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).show()

fun <T> Flow<T>.asDistinctLiveData(): LiveData<T> = asLiveData().distinctUntilChanged()