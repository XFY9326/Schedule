package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.EditError
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.ScheduleManager
import java.util.*

class ScheduleEditViewModel : AbstractViewModel() {
    var isEdit = false
        private set
    private var originalScheduleHashCode: Int? = null
    lateinit var editSchedule: Schedule
        private set

    var courseCostTime = 40
    var breakCostTime = 10
    var scheduleTimeCourseTimeSame = false

    val scheduleSaveComplete = MutableEventLiveData<Long>()
    val scheduleData = MutableLiveData<Schedule>()
    val selectScheduleDate = MutableEventLiveData<Triple<Boolean, Date, WeekDay>>()
    val scheduleSaveFailed = MutableEventLiveData<EditError>()
    val loadAllSchedules = MutableEventLiveData<List<Schedule.Min>>()
    val importScheduleTimes = MutableEventLiveData<Array<ScheduleTime>?>()

    fun selectScheduleDate(isStart: Boolean, date: Date) {
        viewModelScope.launch {
            selectScheduleDate.postEvent(Triple(isStart, date, ScheduleDataStore.firstDayOfWeekFlow.first()))
        }
    }

    fun requestDBScheduleData(scheduleId: Long) {
        isEdit = scheduleId != 0L

        if (!::editSchedule.isInitialized) {
            viewModelScope.launch {
                if (isEdit) {
                    ScheduleDBProvider.db.scheduleDAO.getSchedule(scheduleId).firstOrNull()?.let {
                        editSchedule = it
                        scheduleData.postValue(it)
                    }
                } else {
                    editSchedule = ScheduleManager.createNewSchedule()
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
        viewModelScope.launch {
            ScheduleDBProvider.db.scheduleDAO.deleteSchedule(schedule)
        }
    }

    fun saveSchedule() {
        val cache = editSchedule
        viewModelScope.launch {
            val errorMsg = validateSchedule(cache)
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            importScheduleTimes.postEvent(ScheduleDBProvider.db.scheduleDAO.getSchedule(scheduleId).firstOrNull()?.times)
        }
    }

    private suspend fun validateSchedule(schedule: Schedule): EditError? {
        if (schedule.name.isBlank() || schedule.name.isEmpty()) {
            return EditError.Type.SCHEDULE_NAME_EMPTY.make()
        }

        val maxWeekNum = schedule.maxWeekNum

        if (schedule.startDate >= schedule.endDate) {
            return EditError.Type.SCHEDULE_DATE_ERROR.make()
        }
        if (maxWeekNum <= 0) {
            return EditError.Type.SCHEDULE_MAX_WEEK_NUM_ERROR.make()
        }

        schedule.times.forEachTwo { i1, time1, i2, time2 ->
            if (time1 intersect time2) return EditError.Type.SCHEDULE_TIME_CONFLICT_ERROR.make(i1 + 1, i2 + 1)
        }

        val courses = ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(schedule.scheduleId).first()

        courses.iterateAll { course, courseTime ->
            if (courseTime.classTime.classEndTime > schedule.times.size) {
                return EditError.Type.SCHEDULE_COURSE_NUM_ERROR.make(course.name)
            }
            if (courseTime.weekNum.size > maxWeekNum) {
                return EditError.Type.SCHEDULE_MAX_WEEK_NUM_ERROR.make(course.name)
            }
        }
        return null
    }
}