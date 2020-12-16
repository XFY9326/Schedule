@file:Suppress("unused")

package tool.xfy9326.schedule.kt

import android.app.Dialog
import android.content.res.Resources
import android.graphics.drawable.*
import android.os.Build
import android.text.Editable
import android.util.TypedValue
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.snackbar.Snackbar

fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

fun Int.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

fun Int.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

fun CoordinatorLayout.showShortSnackBar(@StringRes strId: Int, vararg params: Any) =
    Snackbar.make(this, context.getString(strId, *params), Snackbar.LENGTH_SHORT).show()

fun CoordinatorLayout.showLongSnackBar(@StringRes strId: Int, vararg params: Any) =
    Snackbar.make(this, context.getString(strId, *params), Snackbar.LENGTH_LONG).show()

fun CoordinatorLayout.showShortSnackBar(str: String) =
    Snackbar.make(this, str, Snackbar.LENGTH_SHORT).show()

fun CoordinatorLayout.showLongSnackBar(str: String) =
    Snackbar.make(this, str, Snackbar.LENGTH_LONG).show()

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
        override fun onStop(owner: LifecycleOwner) {
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

fun Drawable.startAnimateDrawable() {
    when {
        this is AnimatedVectorDrawable -> start()
        this is AnimatedVectorDrawableCompat -> start()
        this is Animatable -> start()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this is Animatable2 -> start()
        this is Animatable2Compat -> start()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && this is AnimatedImageDrawable -> start()
    }
}

fun Drawable.stopAnimateDrawable() {
    when {
        this is AnimatedVectorDrawable -> stop()
        this is AnimatedVectorDrawableCompat -> stop()
        this is Animatable -> stop()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this is Animatable2 -> stop()
        this is Animatable2Compat -> stop()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && this is AnimatedImageDrawable -> stop()
    }
}

fun Drawable.isAnimatedVectorDrawable() = this is AnimatedVectorDrawable || this is AnimatedVectorDrawableCompat

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