package tool.xfy9326.schedule.ui.vm

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.beans.Course
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
import tool.xfy9326.schedule.tools.ImageHelper
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.CalendarUtils
import tool.xfy9326.schedule.utils.CourseManager
import tool.xfy9326.schedule.utils.CourseTimeUtils
import tool.xfy9326.schedule.utils.ScheduleManager

class ScheduleViewModel : AbstractViewModel() {
    companion object {
        private val currentScheduleId = AppDataStore.currentScheduleIdFlow
        private val currentScheduleFlow = ScheduleManager.getCurrentScheduleFlow()
        val weekNumInfoFlow = currentScheduleFlow.combine(ScheduleDataStore.firstDayOfWeekFlow) { schedule, firstDayOfWeek ->
            CourseTimeUtils.getWeekNum(schedule, firstDayOfWeek) to schedule.maxWeekNum
        }
    }

    val scheduleBuildData = currentScheduleFlow.combine(ScheduleDataStore.scheduleStylesFlow) { schedule, styles ->
        schedule to styles
    }.combineTransform(
        combineTransform = {
            ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(it.first.scheduleId)
        },
        transform = { pair, courses ->
            Triple(pair.first, courses, pair.second)
        }
    ).shareIn(viewModelScope, SharingStarted.Eagerly, 1).asScopeLiveData(viewModelScope)

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
    val toolBarTintColor = ScheduleDataStore.toolBarTintColorFlow.asScopeLiveData(viewModelScope)
    val useLightColorStatusBarColor = ScheduleDataStore.useLightColorStatusBarColorFlow.asScopeLiveData(viewModelScope)
    val scheduleBackground = ScheduleDataStore.scheduleBackgroundBuildBundleFlow.asScopeLiveData(viewModelScope)
    val scheduleShared = MutableEventLiveData<Uri?>()
    val scheduleSharedMutex = Mutex()

    fun scrollToCurrentWeekNum() {
        viewModelScope.launch {
            scrollToWeek.postEvent(weekNumInfoFlow.first().first)
        }
    }

    fun openCurrentScheduleCourseManageActivity() {
        viewModelScope.launch {
            openCourseManageActivity.postEvent(currentScheduleId.first())
        }
    }

    fun showScheduleControlPanel() {
        viewModelScope.launch {
            showScheduleControlPanel.postEvent(weekNumInfoFlow.first())
        }
    }

    fun notifyShowWeekChanged(weekNum: Int) {
        viewModelScope.launch {
            showWeekChanged.postEvent(weekNum to WeekNumType.create(weekNum, weekNumInfoFlow.first().first))
        }
    }

    fun exitAppDirectly() {
        viewModelScope.launch {
            exitAppDirectly.postEvent(AppSettingsDataStore.exitAppDirectlyFlow.first())
        }
    }

    fun shareScheduleImage(context: Context, weekNum: Int) {
        if (scheduleSharedMutex.tryLock()) {
            viewModelScope.launch {
                try {
                    val scheduleId = currentScheduleId.first()
                    val targetWidth = context.resources.displayMetrics.widthPixels
                    val bitmap = CourseManager.generateScheduleImageByWeekNum(context, scheduleId, weekNum, targetWidth)
                    if (bitmap != null) {
                        val uri = if (AppSettingsDataStore.saveImageWhileSharingFlow.first()) {
                            ImageHelper.outputImageToAlbum(context, bitmap)
                        } else {
                            ImageHelper.createShareCacheImage(context, bitmap)
                        }

                        if (uri != null) {
                            scheduleShared.postEvent(uri)
                        } else {
                            scheduleShared.postEvent(null)
                        }
                    } else {
                        scheduleShared.postEvent(null)
                    }
                } finally {
                    scheduleSharedMutex.unlock()
                }
            }
        }
    }

    fun showCourseDetailDialog(courseId: Long, timeId: Long) {
        viewModelScope.launch {
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