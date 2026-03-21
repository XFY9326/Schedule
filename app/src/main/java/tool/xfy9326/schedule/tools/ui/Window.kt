@file:Suppress("unused")

package tool.xfy9326.schedule.tools.ui

import android.graphics.Rect
import android.view.Window
import androidx.annotation.Px
import androidx.core.view.WindowInsetsControllerCompat

@Px
fun Window.getStatusBarHeight(): Int {
    return Rect().let {
        decorView.getWindowVisibleDisplayFrame(it)
        it.top
    }
}

// Light status bar in Android Window means status bar that used in light background, so the status bar color is black.
fun Window.setLightSystemBar(enabled: Boolean) {
    WindowInsetsControllerCompat(this, decorView).apply {
        isAppearanceLightStatusBars = enabled
        isAppearanceLightNavigationBars = enabled
    }
}
