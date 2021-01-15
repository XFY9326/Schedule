@file:Suppress("unused")

package tool.xfy9326.schedule.tools.livedata

import java.lang.ref.WeakReference

open class Event<out T>(content: T) {
    private val weakContentReference = WeakReference(Wrapper(content))
    private val consumedList = ArrayList<Int>()

    val valueWrapper
        get() = weakContentReference.get()

    @Synchronized
    fun isConsumed(tag: String? = null) = tag.hashCode() in consumedList

    @Synchronized
    fun consume(tag: String? = null): Boolean {
        val hashCode = tag.hashCode()
        return if (hashCode in consumedList) {
            false
        } else {
            consumedList.add(hashCode)
            true
        }
    }

    @Synchronized
    fun consumeReset() = consumedList.clear()

    class Wrapper<out T>(val value: T)
}