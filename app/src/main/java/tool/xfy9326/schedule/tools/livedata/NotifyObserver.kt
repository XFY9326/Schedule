package tool.xfy9326.schedule.tools.livedata

import androidx.lifecycle.Observer

/**
 * 通知消费事件的LiveData监听器
 */
class NotifyObserver : Observer<Notify> {

    private val tag: String?
    private var notifyHandler: (() -> Unit)? = null
    private var notifyObserver: Observer<Unit>? = null

    constructor(tag: String? = null, notifyHandler: () -> Unit) {
        this.tag = tag
        this.notifyHandler = notifyHandler
    }

    constructor(tag: String? = null, notifyObserver: Observer<Unit>) {
        this.tag = tag
        this.notifyObserver = notifyObserver
    }

    override fun onChanged(value: Notify) {
        if (value.consume(tag)) {
            notifyHandler?.invoke()
            notifyObserver?.onChanged(Unit)
        }
    }
}