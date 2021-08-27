package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import lib.xfy9326.android.kit.io.IOManager
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.EditError
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.schedule.ScheduleUtils
import java.util.*

class ScheduleEditViewModel : AbstractViewModel() {
    var isEdit = false
        private set
    private var originalScheduleHashCode: Int? = null
    lateinit var editSchedule: Schedule
        private set

    var courseCostTime = IOManager.resources.getInteger(R.integer.default_course_cost_time)
    var breakCostTime = IOManager.resources.getInteger(R.integer.default_break_cost_time)
    var scheduleTimeCourseTimeSame = false

    val scheduleSaveComplete = MutableEventLiveData<Long>()
    val scheduleData = MutableLiveData<Schedule>()
    val selectScheduleDate = MutableEventLiveData<Triple<Boolean, Date, WeekDay>>()
    val scheduleSaveFailed = MutableEventLiveData<EditError>()
    val loadAllSchedules = MutableEventLiveData<List<Schedule.Min>>()
    val importScheduleTimes = MutableEventLiveData<List<ScheduleTime>?>()

    fun selectScheduleDate(isStart: Boolean, date: Date) {
        viewModelScope.launch {
            selectScheduleDate.postEvent(Triple(isStart, date, editSchedule.weekStart))
        }
    }

    fun requestDBScheduleData(scheduleId: Long) {
        isEdit = scheduleId != 0L

        if (!::editSchedule.isInitialized) {
            viewModelScope.launch(Dispatchers.IO) {
                if (isEdit) {
                    ScheduleDBProvider.db.scheduleDAO.getSchedule(scheduleId).firstOrNull()?.let {
                        editSchedule = it
                        scheduleData.postValue(it)
                    }
                } else {
                    editSchedule = ScheduleUtils.createNewSchedule()
                    scheduleData.postValue(editSchedule)
                }
                originalScheduleHashCode = editSchedule.hashCode()
            }
        } else {
            scheduleData.value = editSchedule
        }
    }

    fun hasDataChanged() = originalScheduleHashCode != editSchedule.hashCode()

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch(Dispatchers.IO) {
            ScheduleDBProvider.db.scheduleDAO.deleteSchedule(schedule)
        }
    }

    fun saveSchedule() {
        val cache = editSchedule
        viewModelScope.launch(Dispatchers.IO) {
            val errorMsg = ScheduleUtils.validateSchedule(cache, ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(cache.scheduleId).first())
            if (errorMsg == null) {
                val newId = if (isEdit) {
                    ScheduleDBProvider.db.scheduleDAO.updateSchedule(cache)
                    cache.scheduleId
                } else {
                    isEdit = true
                    ScheduleDBProvider.db.scheduleDAO.putSchedule(cache).also {
                        editSchedule.scheduleId = it
                    }
                }
                originalScheduleHashCode = editSchedule.hashCode()
                scheduleSaveComplete.postEvent(newId)
            } else {
                scheduleSaveFailed.postEvent(errorMsg)
            }
        }
    }

    fun loadAllSchedules() {
        viewModelScope.launch(Dispatchers.IO) {
            var schedules = ScheduleDBProvider.db.scheduleDAO.getScheduleMin().first()
            if (isEdit) {
                schedules = schedules.filter {
                    it.scheduleId != editSchedule.scheduleId
                }
            }
            loadAllSchedules.postEvent(schedules)
        }
    }

    fun importScheduleTimes(scheduleId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            importScheduleTimes.postEvent(ScheduleDBProvider.db.scheduleDAO.getScheduleTimes(scheduleId).firstOrNull()?.times)
        }
    }
}