package tool.xfy9326.schedule.kt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Window
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.core.view.drawToBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
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

fun <T> Flow<T>.asDistinctLiveData(): LiveData<T> = asLiveData().distinctUntilChanged()

fun CharSequence.asStringBuilder(@IntRange(from = 0) moreCapacity: Int? = null): StringBuilder =
    if (moreCapacity == null) {
        StringBuilder(this)
    } else if (moreCapacity >= 0) {
        StringBuilder(length + moreCapacity).also { it.append(this) }
    } else {
        error("Capacity '$moreCapacity' must >= 0!")
    }
