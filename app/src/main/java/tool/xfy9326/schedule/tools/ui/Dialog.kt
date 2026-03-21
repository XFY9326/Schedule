package tool.xfy9326.schedule.tools.ui

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

fun AlertDialog.Builder.show(
    lifecycleOwner: LifecycleOwner,
    onCreate: (AlertDialog) -> Unit = {},
    onDismiss: (DialogInterface) -> Unit = {}
) {
    create().run {
        onCreate(this)
        show(lifecycleOwner, onDismiss)
    }
}

fun AlertDialog.show(
    lifecycleOwner: LifecycleOwner,
    onDismiss: (DialogInterface) -> Unit = {}
) {
    val observer = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            if (isShowing) dismiss()
            owner.lifecycle.removeObserver(this)
        }
    }
    setOnDismissListener {
        onDismiss(it)
        lifecycleOwner.lifecycle.removeObserver(observer)
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    show()
}