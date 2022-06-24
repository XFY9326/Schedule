package tool.xfy9326.schedule.utils

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ColorInt

fun Context.isUsingNightMode(mode: Int = resources.configuration.uiMode) =
    mode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

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
