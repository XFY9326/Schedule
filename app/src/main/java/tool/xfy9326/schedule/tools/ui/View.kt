@file:Suppress("unused")

package tool.xfy9326.schedule.tools.ui

import android.animation.Animator
import android.graphics.Bitmap
import android.graphics.Paint
import android.text.Editable
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.annotation.ColorInt
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.view.children
import androidx.core.view.iterator
import tool.xfy9326.schedule.tools.base.tryCast

fun ViewGroup.setAllEnable(enabled: Boolean) {
    isEnabled = enabled
    for (child in children) {
        if (child is ViewGroup) {
            child.setAllEnable(enabled)
        } else {
            child.isEnabled = enabled
        }
    }
}

fun Editable?.getText(): String? {
    if (this == null) return null
    val str = toString()
    return if (str.isEmpty() || str.isBlank()) {
        null
    } else {
        str
    }
}

fun Menu.setIconTint(@ColorInt colorTint: Int?) {
    iterator().forEach {
        if (colorTint == null) {
            it.icon?.setTintList(null)
        } else {
            it.icon?.setTint(colorTint)
        }
    }
}

fun View.removeSelf() {
    parent.tryCast<ViewGroup?>()?.removeView(this)
}

fun ViewPropertyAnimator.setListener(
    doOnStart: ((Animator) -> Unit)? = null,
    doOnEnd: ((Animator) -> Unit)? = null,
    doOnCancel: ((Animator) -> Unit)? = null,
    doOnRepeat: ((Animator) -> Unit)? = null,
    doOnFinally: ((Animator) -> Unit)? = null,
): ViewPropertyAnimator {
    setListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            doOnStart?.invoke(animation)
        }

        override fun onAnimationEnd(animation: Animator) {
            doOnEnd?.invoke(animation)
            doOnFinally?.invoke(animation)
        }

        override fun onAnimationCancel(animation: Animator) {
            doOnCancel?.invoke(animation)
            doOnFinally?.invoke(animation)
        }

        override fun onAnimationRepeat(animation: Animator) {
            doOnRepeat?.invoke(animation)
        }
    })
    return this
}

val Paint.textBaselineHeight: Float
    get() = fontMetrics.let { (it.descent - it.ascent) / 2f }

fun View.requestLayoutToBitmap(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int,
    @ColorInt backgroundColor: Int? = null,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): Bitmap {
    measure(widthMeasureSpec, heightMeasureSpec)
    layout(0, 0, measuredWidth, measuredHeight)
    requestLayout()
    return createBitmap(measuredWidth, measuredHeight, config).applyCanvas {
        if (backgroundColor == null) {
            background?.draw(this)
        } else {
            drawColor(backgroundColor)
        }
        draw(this)
    }
}