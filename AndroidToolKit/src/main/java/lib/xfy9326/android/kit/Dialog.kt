@file:Suppress("unused")

package lib.xfy9326.android.kit

import android.app.Dialog
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

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


