package tool.xfy9326.schedule.tools.ui

import android.app.Activity
import android.graphics.Point
import android.os.Build
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher

fun Activity.getRealScreenSize(): Pair<Int, Int> =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            windowManager.currentWindowMetrics.bounds.let {
                it.width() to it.height()
            }
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            Point().let {
                @Suppress("DEPRECATION")
                display?.getRealSize(it)
                it.x to it.y
            }
        }

        else -> {
            Point().let {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealSize(it)
                it.x to it.y
            }
        }
    }

fun OnBackPressedCallback.resume(dispatcher: OnBackPressedDispatcher) {
    isEnabled = false
    dispatcher.onBackPressed()
}
