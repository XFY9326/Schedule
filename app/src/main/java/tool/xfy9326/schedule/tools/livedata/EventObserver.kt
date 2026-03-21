package tool.xfy9326.schedule.tools.livedata

import androidx.lifecycle.Observer

/**
 * 消费事件的LiveData监听器
 */
class EventObserver<T> : Observer<Event<T>> {
    private val tag: String?
    private var eventHandler: ((T) -> Unit)? = null
    private var eventObserver: Observer<T>? = null

    constructor(tag: String? = null, eventHandler: (T) -> Unit) {
        this.tag = tag
        this.eventHandler = eventHandler
    }

    constructor(tag: String? = null, eventObserver: Observer<T>) {
        this.tag = tag
        this.eventObserver = eventObserver
    }

    override fun onChanged(value: Event<T>) {
        if (value.consume(tag)) value.valueWrapper?.let {
            eventHandler?.invoke(it.value)
            eventObserver?.onChanged(it.value)
        }
    }
}