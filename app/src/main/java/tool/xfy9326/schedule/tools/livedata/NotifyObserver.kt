package tool.xfy9326.schedule.tools.livedata

import androidx.lifecycle.Observer

class NotifyObserver(private val tag: String? = null, private val notifyHandler: () -> Unit) : Observer<Notify> {
    override fun onChanged(t: Notify) {
        if (t.consume(tag)) notifyHandler.invoke()
    }
}