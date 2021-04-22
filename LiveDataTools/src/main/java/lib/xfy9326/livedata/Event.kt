@file:Suppress("unused")

package lib.xfy9326.livedata

import java.lang.ref.WeakReference

/**
 * 可消费的事件，通过tag可以区分消费对象
 * 1. 传递数据是弱引用
 * 2. 可以防止LiveData数据倒灌
 */
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

    @Synchronized
    fun clearEvent() = weakContentReference.clear()

    class Wrapper<out T>(val value: T)
}