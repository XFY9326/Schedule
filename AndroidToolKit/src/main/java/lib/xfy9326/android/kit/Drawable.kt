@file:Suppress("unused", "ObsoleteSdkInt")

package lib.xfy9326.android.kit

import android.graphics.drawable.*
import android.os.Build
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

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