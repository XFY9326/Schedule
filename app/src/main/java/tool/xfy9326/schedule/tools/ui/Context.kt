@file:Suppress("unused")

package tool.xfy9326.schedule.tools.ui

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

@Suppress("NOTHING_TO_INLINE")
inline fun Context.getColorCompat(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

@Suppress("NOTHING_TO_INLINE")
inline fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? = ContextCompat.getDrawable(this, id)

@Suppress("NOTHING_TO_INLINE")
inline fun Context.getStringArray(@ArrayRes id: Int): Array<String> = resources.getStringArray(id)

@Suppress("NOTHING_TO_INLINE")
inline fun Context.getIntArray(@ArrayRes id: Int): IntArray = resources.getIntArray(id)
