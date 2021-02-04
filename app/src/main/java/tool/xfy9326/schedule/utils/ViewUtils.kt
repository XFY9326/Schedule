package tool.xfy9326.schedule.utils

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.ui.dialog.CrashViewDialog

object ViewUtils {
    fun showCourseAdapterErrorSnackBar(activity: AppCompatActivity, coordinatorLayout: CoordinatorLayout, exception: CourseAdapterException) {
        Snackbar.make(coordinatorLayout, exception.getText(activity), Snackbar.LENGTH_LONG)
            .setActionTextColor(Color.RED)
            .setAction(R.string.details) {
                CrashViewDialog.showDialog(activity.supportFragmentManager, exception.stackTraceToString(), false)
            }.show()
    }
}