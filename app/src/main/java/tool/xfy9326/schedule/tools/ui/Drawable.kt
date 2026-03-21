@file:Suppress("unused")

package tool.xfy9326.schedule.tools.ui

import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

fun Drawable.tryStartAnimation() {
    when (this) {
        is AnimatedVectorDrawable -> start()
        is AnimatedVectorDrawableCompat -> start()
        is Animatable -> start()
    }
}

fun Drawable.tryStopAnimation() {
    when (this) {
        is AnimatedVectorDrawable -> stop()
        is AnimatedVectorDrawableCompat -> stop()
        is Animatable -> stop()
    }
}