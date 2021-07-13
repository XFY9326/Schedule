@file:Suppress("unused")

package tool.xfy9326.schedule.kt

import android.animation.Animator
import android.app.Dialog
import android.content.res.Resources
import android.graphics.drawable.*
import android.os.Build
import android.text.Editable
import android.util.TypedValue
import android.view.*
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.children
import androidx.core.view.iterator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.snackbar.Snackbar
import tool.xfy9326.schedule.beans.SystemBarAppearance

fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

fun Int.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

fun Int.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

fun CoordinatorLayout.showSnackBar(@StringRes strId: Int, vararg params: Any, showLong: Boolean = false) =
    Snackbar.make(this, context.getString(strId, *params), if (showLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).show()

fun CoordinatorLayout.showSnackBar(str: String, showLong: Boolean = false) =
    Snackbar.make(this, str, if (showLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).show()

fun Dialog.setWindowPercent(widthPercent: Double = -1.0, heightPercent: Double = -1.0) {
    context.resources?.displayMetrics?.let {
        val width = if (widthPercent < 0) ViewGroup.LayoutParams.WRAP_CONTENT else (it.widthPixels * widthPercent).toInt()
        val height = if (heightPercent < 0) ViewGroup.LayoutParams.WRAP_CONTENT else (it.heightPixels * heightPercent).toInt()
        window?.setLayout(width, height)
    }
}

fun AlertDialog.Builder.show(lifecycleOwner: LifecycleOwner) {
    val dialog = create()
    val observer = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            if (dialog.isShowing) dialog.dismiss()
            owner.lifecycle.removeObserver(this)
        }
    }
    setOnDismissListener {
        lifecycleOwner.lifecycle.removeObserver(observer)
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    dialog.show()
}

fun Drawable.tryStartAnimateDrawable() {
    when {
        this is AnimatedVectorDrawable -> start()
        this is AnimatedVectorDrawableCompat -> start()
        this is Animatable -> start()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this is Animatable2 -> start()
        this is Animatable2Compat -> start()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && this is AnimatedImageDrawable -> start()
    }
}

fun Drawable.tryStopAnimateDrawable() {
    when {
        this is AnimatedVectorDrawable -> stop()
        this is AnimatedVectorDrawableCompat -> stop()
        this is Animatable -> stop()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this is Animatable2 -> stop()
        this is Animatable2Compat -> stop()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && this is AnimatedImageDrawable -> stop()
    }
}

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

fun Window.setSystemBarAppearance(systemBarAppearance: SystemBarAppearance) {
    when (systemBarAppearance) {
        SystemBarAppearance.FOLLOW_THEME -> setLightSystemBar(!decorView.context.isUsingNightMode())
        SystemBarAppearance.USE_LIGHT -> setLightSystemBar(true)
        SystemBarAppearance.USE_DARK -> setLightSystemBar(false)
    }
}

// Light status bar in Android Window means status bar that used in light background, so the status bar color is black.
private fun Window.setLightSystemBar(enabled: Boolean) {
    WindowInsetsControllerCompat(this, decorView).apply {
        isAppearanceLightStatusBars = enabled
        isAppearanceLightNavigationBars = enabled
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

fun WebView.bindLifeCycle(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            onResume()
        }

        override fun onPause(owner: LifecycleOwner) {
            onPause()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            try {
                removeSelf()
                stopLoading()
                settings.javaScriptEnabled = false
                removeAllViews()
                destroy()
            } catch (e: Exception) {
                // Ignore
            }
            lifecycleOwner.lifecycle.removeObserver(this)
        }
    })
}

fun WebView.clearAll(cookies: Boolean = true, webStorage: Boolean = true) {
    settings.javaScriptEnabled = false
    clearHistory()
    clearFormData()
    clearMatches()
    clearSslPreferences()
    clearCache(true)
    if (cookies) {
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
    }
    if (webStorage) {
        WebStorage.getInstance().deleteAllData()
    }
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
