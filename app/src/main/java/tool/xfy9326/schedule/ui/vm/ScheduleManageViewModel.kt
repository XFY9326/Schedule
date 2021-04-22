package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import lib.xfy9326.livedata.MutableNotifyLiveData
import lib.xfy9326.livedata.postNotify
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.asDistinctLiveData
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

class ScheduleManageViewModel : AbstractViewModel() {
    companion object {
        private val schedules = ScheduleDBProvider.db.scheduleDAO.getSchedules()
        private val schedulesDataFlow = AppDataStore.currentScheduleIdFlow.combine(schedules) { schedules, id ->
            id to schedules
        }
    }

    val schedules = schedulesDataFlow.asDistinctLiveData()
    val setCurrentScheduleSuccess = MutableNotifyLiveData()

    fun setCurrentSchedule(scheduleId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDataStore.setCurrentScheduleId(scheduleId)
            setCurrentScheduleSuccess.postNotify()
        }
    }
}