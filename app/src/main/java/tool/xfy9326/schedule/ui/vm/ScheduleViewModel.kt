package tool.xfy9326.schedule.ui.vm

import androidx.collection.SparseArrayCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleBuildBundle
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.WeekNumType
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.asScopeLiveData
import tool.xfy9326.schedule.kt.combineTransform
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.CalendarUtils
import tool.xfy9326.schedule.utils.CourseManager
import tool.xfy9326.schedule.utils.CourseTimeUtils
import tool.xfy9326.schedule.utils.ScheduleManager

class ScheduleViewModel : AbstractViewModel() {
    companion object {
        private val currentScheduleId = AppDataStore.currentScheduleIdFlow
        private val currentScheduleFlow = ScheduleManager.getCurrentScheduleFlow()
        private val scheduleBuildData = currentScheduleFlow.combine(ScheduleDataStore.scheduleStylesFlow) { schedule, styles ->
            schedule to styles
        }.combineTransform(
            combineTransform = {
                ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(it.first.scheduleId)
            },
            transform = { pair, courses ->
                Triple(pair.first, courses, pair.second)
            }
        ).shareIn(GlobalScope, SharingStarted.Eagerly, 1)
        val weekNumInfoFlow = currentScheduleFlow.combine(ScheduleDataStore.firstDayOfWeekFlow) { schedule, firstDayOfWeek ->
            CourseTimeUtils.getWeekNum(schedule, firstDayOfWeek) to schedule.maxWeekNum
        }

        suspend fun preload() {
            scheduleBuildData.firstOrNull()
        }
    }

    var currentScrollPosition: Int? = null

    val nowDay = ScheduleDataStore.firstDayOfWeekFlow.map {
        CalendarUtils.getDay(firstDayOfWeek = it)
    }.asScopeLiveData(viewModelScope)
    val weekNumInfo = weekNumInfoFlow.asScopeLiveData(viewModelScope)
    val scrollToWeek = MutableEventLiveData<Int>()
    val showWeekChanged = MutableEventLiveData<Pair<Int, WeekNumType>>()
    val showScheduleControlPanel = MutableEventLiveData<Pair<Int, Int>>()
    val showCourseDetailDialog = MutableEventLiveData<Triple<Array<ScheduleTime>, Course, Long>>()
    val openCourseManageActivity = MutableEventLiveData<Long>()
    val exitAppDirectly = MutableEventLiveData<Boolean>()
    private val scheduleBuildBundleMap = SparseArrayCompat<LiveData<ScheduleBuildBundle>>()

    fun scrollToCurrentWeekNum() {
        viewModelScope.launch(Dispatchers.Default) {
            scrollToWeek.postEvent(weekNumInfoFlow.first().first)
        }
    }

    fun openCurrentScheduleCourseManageActivity() {
        viewModelScope.launch(Dispatchers.Default) {
            openCourseManageActivity.postEvent(currentScheduleId.first())
        }
    }

    fun showScheduleControlPanel() {
        viewModelScope.launch(Dispatchers.Default) {
            showScheduleControlPanel.postEvent(weekNumInfoFlow.first())
        }
    }

    fun notifyShowWeekChanged(weekNum: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            showWeekChanged.postEvent(weekNum to WeekNumType.create(weekNum, weekNumInfoFlow.first().first))
        }
    }

    fun getScheduleBuildBundleLiveData(weekNum: Int): LiveData<ScheduleBuildBundle> {
        return if (scheduleBuildBundleMap.containsKey(weekNum)) {
            scheduleBuildBundleMap[weekNum]!!
        } else {
            scheduleBuildData.map {
                ScheduleBuildBundle(CourseManager.getScheduleViewDataByWeek(weekNum, it.first, it.second, it.third.showNotThisWeekCourse), it.third)
            }.asScopeLiveData(viewModelScope).also {
                scheduleBuildBundleMap.put(weekNum, it)
            }
        }
    }

    fun exitAppDirectly() {
        viewModelScope.launch {
            exitAppDirectly.postEvent(AppSettingsDataStore.exitAppDirectlyFlow.first())
        }
    }

    fun showCourseDetailDialog(courseId: Long, timeId: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            currentScheduleFlow.combine(ScheduleDBProvider.db.scheduleDAO.getScheduleCourse(courseId)) { schedule, course ->
                if (course == null) {
                    null
                } else {
                    schedule.times to course
                }
            }.first()?.let {
                showCourseDetailDialog.postEvent(Triple(it.first, it.second, timeId))
            }
        }
    }
}