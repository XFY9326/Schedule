@file:Suppress("unused")

package tool.xfy9326.schedule.tools.livedata

open class Event<out T>(val content: T) {
    private val consumedList = ArrayList<Int>()

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
}