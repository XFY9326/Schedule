package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import io.github.xfy9326.atools.livedata.MutableNotifyLiveData
import io.github.xfy9326.atools.livedata.postNotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.asDistinctLiveData

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