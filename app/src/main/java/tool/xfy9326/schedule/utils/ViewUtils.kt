package tool.xfy9326.schedule.utils

import android.app.Dialog
import android.view.ViewGroup

fun Dialog.setWindowPercent(widthPercent: Double = -1.0, heightPercent: Double = -1.0) {
    context.resources?.displayMetrics?.let {
        val width = if (widthPercent < 0) ViewGroup.LayoutParams.WRAP_CONTENT else (it.widthPixels * widthPercent).toInt()
        val height = if (heightPercent < 0) ViewGroup.LayoutParams.WRAP_CONTENT else (it.heightPixels * heightPercent).toInt()
        window?.setLayout(width, height)
    }
}