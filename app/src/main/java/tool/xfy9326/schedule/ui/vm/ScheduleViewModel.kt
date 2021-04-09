package tool.xfy9326.schedule.ui.vm

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.beans.BatchResult
import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.WeekNumType
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.io.utils.ImageUtils
import tool.xfy9326.schedule.kt.asDistinctLiveData
import tool.xfy9326.schedule.kt.withTryLock
import tool.xfy9326.schedule.tools.DisposableValue
import tool.xfy9326.schedule.tools.livedata.MutableEventLiveData
import tool.xfy9326.schedule.tools.livedata.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.CalendarUtils
import tool.xfy9326.schedule.utils.ScheduleSyncHelper
import tool.xfy9326.schedule.utils.ics.ScheduleICSHelper
import tool.xfy9326.schedule.utils.schedule.ScheduleUtils
import tool.xfy9326.schedule.utils.view.ScheduleViewDataProcessor
import tool.xfy9326.schedule.utils.view.ScheduleViewHelper
import java.util.concurrent.atomic.AtomicBoolean

class ScheduleViewModel : AbstractViewModel() {
    val weekNumInfo = ScheduleViewDataProcessor.weekNumInfoFlow.asDistinctLiveData()
    val scheduleBuildData = ScheduleViewDataProcessor.scheduleBuildDataFlow.asDistinctLiveData()
    val scheduleBackground = ScheduleViewDataProcessor.scheduleBackgroundFlow.asDistinctLiveData()

    val nowDay = MutableLiveData<Day>()
    val scrollToWeek = MutableEventLiveData<Int>()
    val showWeekChanged = MutableEventLiveData<Pair<Int, WeekNumType>>()
    val showScheduleControlPanel = MutableEventLiveData<Pair<Int, Int>>()
    val showCourseDetailDialog = MutableEventLiveData<Pair<Long, Long>>()
    val openCourseManageActivity = MutableEventLiveData<Long>()
    val exitAppDirectly = MutableEventLiveData<Boolean>()
    val toolBarTintColor = ScheduleDataStore.toolBarTintColorFlow.asDistinctLiveData()
    val useLightColorSystemBarColor = ScheduleDataStore.useLightColorSystemBarColorFlow.asDistinctLiveData()

    val scheduleShared = MutableEventLiveData<Uri?>()
    val selectScheduleForExportingICS = MutableEventLiveData<List<Schedule.Min>>()
    val iceExportStatus = MutableEventLiveData<Boolean>()
    val syncToCalendarStatus = MutableEventLiveData<BatchResult>()
    val onlineCourseImportEnabled = AppSettingsDataStore.enableOnlineCourseImportFlow.asDistinctLiveData()

    val nightModeChangeOldSurface = DisposableValue<Bitmap>()
    val nightModeChanging = AtomicBoolean(false)
    val waitExportScheduleId = DisposableValue<Long>()

    var currentScrollPosition: Int? = null

    private val scheduleSharedMutex = Mutex()

    override fun onViewInitialized(firstInitialize: Boolean) {
        if (firstInitialize) {
            updateNowDay()
        }
    }

    private fun updateNowDay() {
        nowDay.value = CalendarUtils.getDay()
    }

    fun scrollToCurrentWeekNum() {
        viewModelScope.launch(Dispatchers.IO) {
            scrollToWeek.postEvent(ScheduleViewDataProcessor.weekNumFlow.first())
        }
    }

    fun openCurrentScheduleCourseManageActivity() {
        viewModelScope.launch(Dispatchers.IO) {
            openCourseManageActivity.postEvent(AppDataStore.currentScheduleIdFlow.first())
        }
    }

    fun showScheduleControlPanel() {
        viewModelScope.launch(Dispatchers.IO) {
            showScheduleControlPanel.postEvent(ScheduleViewDataProcessor.weekNumInfoFlow.first())
        }
    }

    fun notifyShowWeekChanged(weekNum: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            showWeekChanged.postEvent(weekNum to WeekNumType.create(weekNum, ScheduleViewDataProcessor.weekNumFlow.first()))
        }
    }

    fun exitAppDirectly() {
        viewModelScope.launch(Dispatchers.IO) {
            exitAppDirectly.postEvent(AppSettingsDataStore.exitAppDirectlyFlow.first())
        }
    }

    fun selectScheduleForExportingICS() {
        viewModelScope.launch(Dispatchers.IO) {
            selectScheduleForExportingICS.postEvent(ScheduleDBProvider.db.scheduleDAO.getScheduleMin().first())
        }
    }

    fun syncToCalendar() {
        viewModelScope.launch(Dispatchers.Default) {
            ScheduleSyncHelper.syncCalendar()?.let(syncToCalendarStatus::postEvent)
        }
    }

    fun exportICS(outputUri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
            val scheduleId = waitExportScheduleId.read()
            if (scheduleId != null) {
                val schedule = ScheduleDBProvider.db.scheduleDAO.getSchedule(scheduleId).first()
                if (schedule != null) {
                    val courses = ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(scheduleId).first()
                    iceExportStatus.postEvent(ScheduleICSHelper(schedule, courses).dumpICS(outputUri))
                } else {
                    iceExportStatus.postEvent(false)
                }
            } else {
                iceExportStatus.postEvent(false)
            }
        }
    }

    fun shareScheduleImage(weekNum: Int, targetWidth: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            scheduleSharedMutex.withTryLock {
                val scheduleId = AppDataStore.currentScheduleIdFlow.first()
                val waterMark = AppSettingsDataStore.drawWaterMarkOnScheduleImageFlow.first()
                val bitmap = ScheduleViewHelper.generateScheduleImageByWeekNum(scheduleId, weekNum, targetWidth, waterMark)
                if (bitmap != null) {
                    val uri = if (AppSettingsDataStore.saveImageWhileSharingFlow.first()) {
                        ImageUtils.outputImageToAlbum(bitmap)
                    } else {
                        ImageUtils.createShareCacheImage(bitmap)
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
        }
    }

    fun showCourseDetailDialog(courseId: Long, timeId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            ScheduleUtils.currentScheduleFlow.combine(ScheduleDBProvider.db.scheduleDAO.getScheduleCourse(courseId)) { schedule, course ->
                if (course == null) {
                    null
                } else {
                    schedule.times to course
                }
            }.first()?.let {
                showCourseDetailDialog.postEvent(courseId to timeId)
            }
        }
    }
}