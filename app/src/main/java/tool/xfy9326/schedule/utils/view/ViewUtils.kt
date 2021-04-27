package tool.xfy9326.schedule.utils.view

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.JSConfigException
import tool.xfy9326.schedule.kt.getDeepStackTraceString
import tool.xfy9326.schedule.ui.dialog.CrashViewDialog

object ViewUtils {
    fun showCourseAdapterErrorSnackBar(activity: AppCompatActivity, coordinatorLayout: CoordinatorLayout, exception: CourseAdapterException) {
        Snackbar.make(coordinatorLayout, exception.getText(activity), Snackbar.LENGTH_LONG)
            .setActionTextColor(Color.RED)
            .setAction(R.string.details) {
                CrashViewDialog.showDialog(activity.supportFragmentManager, exception.getDeepStackTraceString(), false)
            }.show()
    }

    fun showJSConfigErrorSnackBar(activity: AppCompatActivity, coordinatorLayout: CoordinatorLayout, exception: JSConfigException) {
        Snackbar.make(coordinatorLayout, exception.getText(activity), Snackbar.LENGTH_LONG)
            .setActionTextColor(Color.RED)
            .setAction(R.string.details) {
                CrashViewDialog.showDialog(activity.supportFragmentManager, exception.getDeepStackTraceString(), false)
            }.show()
    }

    fun buildBackground(@ColorInt contentColorInt: Int, @ColorInt rippleColorInt: Int, @Px radius: Float): Drawable {
        val content = GradientDrawable().apply {
            if (radius != 0f) cornerRadius = radius
            setColor(contentColorInt)
        }
        return RippleDrawable(ColorStateList.valueOf(rippleColorInt), content, null)
    }
}