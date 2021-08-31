@file:Suppress("unused")

package lib.xfy9326.android.kit

import android.animation.Animator
import android.content.res.Resources
import android.graphics.Paint
import android.text.Editable
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.view.children
import androidx.core.view.iterator
import lib.xfy9326.kit.tryCast

@Px
fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

@Px
fun Int.dpToPx() = dpToPxFloat().toInt()

@Px
fun Int.dpToPxFloat() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)

@Px
fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

@Px
fun Int.spToPx() = spToPxFloat().toInt()

@Px
fun Int.spToPxFloat() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics)

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

fun View.removeSelf() = parent.tryCast<ViewGroup?>()?.removeView(this)

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
    get() {
        val font = fontMetrics
        return (font.descent - font.ascent) / 2f
    }
