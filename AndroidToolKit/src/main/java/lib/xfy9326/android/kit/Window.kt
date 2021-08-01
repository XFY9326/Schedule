@file:Suppress("unused")

package lib.xfy9326.android.kit

import android.graphics.Rect
import android.view.Window
import androidx.annotation.Px
import androidx.core.view.WindowInsetsControllerCompat

@Px
fun Window.getStatusBarHeight(): Int {
    val rect = Rect()
    decorView.getWindowVisibleDisplayFrame(rect)
    return rect.top
}

// Light status bar in Android Window means status bar that used in light background, so the status bar color is black.
fun Window.setLightSystemBar(enabled: Boolean) {
    WindowInsetsControllerCompat(this, decorView).apply {
        isAppearanceLightStatusBars = enabled
        isAppearanceLightNavigationBars = enabled
    }
}