package tool.xfy9326.schedule.tools.livedata

import androidx.lifecycle.Observer

class EventObserver<T>(private val tag: String? = null, private val eventHandler: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(t: Event<T>) {
        if (t.consume(tag)) eventHandler.invoke(t.content)
    }
}