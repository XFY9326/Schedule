package tool.xfy9326.schedule.utils.view

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import lib.xfy9326.kit.getDeepStackTraceString
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.NightMode.Companion.modeInt
import tool.xfy9326.schedule.content.base.CourseImportException
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.ui.dialog.CrashViewDialog
import tool.xfy9326.schedule.utils.IntentUtils

object ViewUtils {
    fun showCourseImportErrorSnackBar(activity: AppCompatActivity, coordinatorLayout: CoordinatorLayout, exception: CourseImportException) {
        Snackbar.make(coordinatorLayout, exception.getText(activity), Snackbar.LENGTH_LONG)
            .setActionTextColor(Color.RED)
            .setAction(R.string.details) {
                CrashViewDialog.showDialog(activity.supportFragmentManager, exception.getDeepStackTraceString(), false)
            }.show()
    }

    fun showScheduleImageSaveSnackBar(coordinatorLayout: CoordinatorLayout, uri: Uri) {
        Snackbar.make(coordinatorLayout, R.string.generate_save_schedule_success, Snackbar.LENGTH_LONG)
            .setActionTextColor(Color.RED)
            .setAction(R.string.see) {
                IntentUtils.seeImage(coordinatorLayout.context, uri)
            }.show()
    }

    fun buildBackground(@ColorInt contentColorInt: Int, @ColorInt rippleColorInt: Int, @Px radius: Float): Drawable {
        val content = GradientDrawable().apply {
            if (radius != 0f) cornerRadius = radius
            setColor(contentColorInt)
        }
        return RippleDrawable(ColorStateList.valueOf(rippleColorInt), content, null)
    }

    fun initNightMode() {
        runBlocking {
            AppCompatDelegate.setDefaultNightMode(AppSettingsDataStore.nightModeTypeFlow.first().modeInt)
        }
    }
}