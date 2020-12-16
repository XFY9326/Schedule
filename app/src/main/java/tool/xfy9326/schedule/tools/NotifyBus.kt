package tool.xfy9326.schedule.tools

import tool.xfy9326.schedule.kt.MutableNotifyLiveData
import java.util.*

object NotifyBus {
    private val liveDataMap = Hashtable<NotifyType, MutableNotifyLiveData>(NotifyType.values().size)

    init {
        for (value in NotifyType.values()) {
            liveDataMap[value] = MutableNotifyLiveData()
        }
    }

    operator fun get(notifyType: NotifyType) = liveDataMap[notifyType]!!
}