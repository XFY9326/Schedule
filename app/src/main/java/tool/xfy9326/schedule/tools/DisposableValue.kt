package tool.xfy9326.schedule.tools

class DisposableValue<T>(initialValue: T? = null, private val sync: Boolean = false) {
    private var value = initialValue

    fun write(value: T) {
        if (sync) {
            synchronized(this) {
                this.value = value
            }
        } else {
            this.value = value
        }
    }

    fun read(): T? {
        if (sync) {
            synchronized(this) {
                val temp = this.value
                this.value = null
                return temp
            }
        } else {
            val temp = this.value
            this.value = null
            return temp
        }
    }

    fun consume() {
        read()
    }
}