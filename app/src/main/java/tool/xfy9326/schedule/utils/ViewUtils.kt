package tool.xfy9326.schedule.utils

import android.app.Dialog
import android.os.Build
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max

fun Dialog.setWindowPercent(widthPercent: Double = -1.0, heightPercent: Double = -1.0) {
    context.resources?.displayMetrics?.let {
        val width = if (widthPercent < 0) ViewGroup.LayoutParams.WRAP_CONTENT else (it.widthPixels * widthPercent).toInt()
        val height = if (heightPercent < 0) ViewGroup.LayoutParams.WRAP_CONTENT else (it.heightPixels * heightPercent).toInt()
        window?.setLayout(width, height)
    }
}

fun View.getSystemBarBottomInsets(): Int =
    ViewCompat.getRootWindowInsets(this)?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: 0

fun View.getBottomScreenCornerMargin(): Int? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val bottomLeftCorner = rootWindowInsets?.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT) ?: return null
        val bottomRightCorner = rootWindowInsets?.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT) ?: return null
        return max(bottomLeftCorner.radius, bottomRightCorner.radius)
    } else {
        return null
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

fun BottomSheetDialogFragment.consumeBottomInsets() {
    dialog?.window?.apply {
        WindowCompat.setDecorFitsSystemWindows(this, false)
        ViewCompat.setOnApplyWindowInsetsListener(decorView) { v, insets ->
            val systemBarInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            v.findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)?.updatePadding(
                bottom = systemBarInset.bottom,
                left = systemBarInset.left + cutoutInsets.left,
                right = systemBarInset.right + cutoutInsets.right
            )
            WindowInsetsCompat.CONSUMED
        }
    }
}

fun CoordinatorLayout.showSnackBar(@StringRes strId: Int, vararg params: Any, showLong: Boolean = false) =
    showSnackBar(context.getString(strId, *params), showLong)

fun CoordinatorLayout.showSnackBar(str: String, showLong: Boolean = false) =
    Snackbar.make(this, str, if (showLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).show()

