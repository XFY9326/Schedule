@file:Suppress("unused")

package tool.xfy9326.schedule.kt

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.*
import android.os.Build
import android.text.Editable
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.core.view.iterator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.snackbar.Snackbar
import tool.xfy9326.schedule.R

fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

fun Int.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

fun Int.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

fun CoordinatorLayout.showSnackBar(@StringRes strId: Int, vararg params: Any, showLong: Boolean = false) =
    Snackbar.make(this, context.getString(strId, *params), if (showLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).show()

fun CoordinatorLayout.showSnackBar(str: String, showLong: Boolean = false) =
    Snackbar.make(this, str, if (showLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).show()

fun Dialog.setWindowWidthPercent(widthPercent: Double): Int? {
    context.resources?.displayMetrics?.let {
        val width = (it.widthPixels * widthPercent).toInt()
        window?.apply {
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return width
    }
    return null
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

@Suppress("DEPRECATION")
fun Window.enableLightSystemBar(context: Context, enabled: Boolean) {
    when {
//        New api setSystemBarsBehavior() is not stable even in Android R
//        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
//            insetsController?.apply {
//                if (enabled) {
//                    setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
//                    setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
//                } else {
//                    setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
//                    setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
//                }
//            }
//        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            decorView.systemUiVisibility = if (enabled) {
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }
        else -> {
            if (enabled) {
                decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                navigationBarColor = context.getColorCompat(R.color.light_navigation_bar)
            } else {
                decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                context.getColorCompat(R.color.not_light_navigation_bar)
            }
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        navigationBarDividerColor = Color.TRANSPARENT
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

fun View.removeSelf() = (parent as? ViewGroup)?.removeView(this)

fun WebView.bindLifeCycle(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            onResume()
            resumeTimers()
        }

        override fun onPause(owner: LifecycleOwner) {
            pauseTimers()
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
                e.printStackTrace()
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