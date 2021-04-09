package tool.xfy9326.schedule.tools

import java.lang.ref.WeakReference

class DisposableValue<T>(initialValue: T? = null, private val sync: Boolean = false) {
    private var value = WeakReference(initialValue)

    fun write(value: T) {
        if (sync) {
            synchronized(this) {
                this.value = WeakReference(value)
            }
        } else {
            this.value = WeakReference(value)
        }
    }

    fun read(): T? {
        if (sync) {
            synchronized(this) {
                return internalRead()
            }
        } else {
            return internalRead()
        }
    }

    private fun internalRead(): T? {
        val temp = this.value.get()
        this.value.clear()
        return temp
    }

    fun consume() {
        read()
    }
}