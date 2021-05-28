package tool.xfy9326.schedule.kt

import android.view.View

@JvmSynthetic
fun View.setOnSingleClickListener(block: (View?) -> Unit) {
    setOnClickListener(object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            block(v)
        }
    })
}

fun View.setOnSingleClickListener(listener: View.OnClickListener) {
    setOnClickListener(object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            listener.onClick(v)
        }
    })
}

abstract class OnSingleClickListener : View.OnClickListener {
    companion object {
        private const val MIN_CLICK_INTERVAL = 500L
    }

    private var lastClickTime = 0L

    override fun onClick(v: View?) {
        val currentMills = System.currentTimeMillis()
        if (currentMills - lastClickTime > MIN_CLICK_INTERVAL) {
            onSingleClick(v)
        }
        lastClickTime = currentMills
    }

    protected abstract fun onSingleClick(v: View?)
}