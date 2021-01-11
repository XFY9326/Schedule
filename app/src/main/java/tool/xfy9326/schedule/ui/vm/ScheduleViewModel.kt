package tool.xfy9326.schedule.ui.vm

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.tools.DisposableValue
import tool.xfy9326.schedule.tools.ImageHelper
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.*
import java.util.concurrent.atomic.AtomicBoolean

class ScheduleViewModel : AbstractViewModel() {
    companion object {
        private val currentScheduleId = AppDataStore.currentScheduleIdFlow
        private val currentScheduleFlow = ScheduleManager.getCurrentScheduleFlow()
        private val weekNumInfoFlow = currentScheduleFlow.combine(ScheduleDataStore.firstDayOfWeekFlow) { schedule, firstDayOfWeek ->
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
    ).distinctUntilChanged().shareIn(GlobalScope, SharingStarted.Eagerly, 1).asDistinctLiveData()
    val weekNumInfo = weekNumInfoFlow.shareIn(GlobalScope, SharingStarted.Eagerly, 1).asDistinctLiveData()

    var currentScrollPosition: Int? = null

    val nowDay = ScheduleDataStore.firstDayOfWeekFlow.map {
        CalendarUtils.getDay(firstDayOfWeek = it)
    }.asDistinctLiveData()
    val scrollToWeek = MutableEventLiveData<Int>()
    val showWeekChanged = MutableEventLiveData<Pair<Int, WeekNumType>>()
    val showScheduleControlPanel = MutableEventLiveData<Pair<Int, Int>>()
    val showCourseDetailDialog = MutableEventLiveData<Triple<Array<ScheduleTime>, Course, Long>>()
    val openCourseManageActivity = MutableEventLiveData<Long>()
    val exitAppDirectly = MutableEventLiveData<Boolean>()
    val toolBarTintColor = ScheduleDataStore.toolBarTintColorFlow.asDistinctLiveData()
    val useLightColorStatusBarColor = ScheduleDataStore.useLightColorStatusBarColorFlow.asDistinctLiveData()
    val scheduleBackground = ScheduleDataStore.scheduleBackgroundBuildBundleFlow.asDistinctLiveData()
    val scheduleShared = MutableEventLiveData<Uri?>()
    val selectScheduleForExportingICS = MutableEventLiveData<List<Schedule.Min>>()
    val iceExportStatus = MutableEventLiveData<Boolean>()
    val syncToCalendarStatus = MutableEventLiveData<ScheduleSync.Result>()

    var nightModeChangeOldSurface: Bitmap? = null
    val nightModeChanging = AtomicBoolean(false)
    val waitExportScheduleId = DisposableValue<Long>()

    private val scheduleSharedMutex = Mutex()

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

    fun selectScheduleForExportingICS() {
        viewModelScope.launch {
            selectScheduleForExportingICS.postEvent(ScheduleDBProvider.db.scheduleDAO.getScheduleMin().first())
        }
    }

    fun syncToCalendar(context: Context) {
        val weakContext = context.weak()
        viewModelScope.launch {
            weakContext.get()?.let {
                ScheduleSyncHelper.syncCalendar(it)?.let(syncToCalendarStatus::postEvent)
            }
        }
    }

    fun exportICS(context: Context, outputUri: Uri) {
        val weakContext = context.weak()
        viewModelScope.launch {
            val scheduleId = waitExportScheduleId.read()
            if (scheduleId != null) {
                val schedule = ScheduleDBProvider.db.scheduleDAO.getSchedule(scheduleId).first()
                if (schedule != null) {
                    val courses = ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(scheduleId).first()
                    val firstDayOfWeek = ScheduleDataStore.firstDayOfWeekFlow.first()
                    weakContext.get()?.let {
                        iceExportStatus.postEvent(ScheduleICSHelper(schedule, courses, firstDayOfWeek).dumpICS(it, outputUri))
                    }
                } else {
                    iceExportStatus.postEvent(false)
                }
            } else {
                iceExportStatus.postEvent(false)
            }
        }
    }

    fun shareScheduleImage(context: Context, weekNum: Int) {
        val weakContext = context.weak()
        if (scheduleSharedMutex.tryLock()) {
            viewModelScope.launch {
                try {
                    weakContext.get()?.let {
                        val scheduleId = currentScheduleId.first()
                        val targetWidth = it.resources.displayMetrics.widthPixels
                        val bitmap = CourseManager.generateScheduleImageByWeekNum(it, scheduleId, weekNum, targetWidth)
                        if (bitmap != null) {
                            val uri = if (AppSettingsDataStore.saveImageWhileSharingFlow.first()) {
                                ImageHelper.outputImageToAlbum(it, bitmap)
                            } else {
                                ImageHelper.createShareCacheImage(it, bitmap)
                            }

                            if (uri != null) {
                                scheduleShared.postEvent(uri)
                            } else {
                                scheduleShared.postEvent(null)
                            }
                        } else {
                            scheduleShared.postEvent(null)
                        }
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