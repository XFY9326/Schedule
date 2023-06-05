package tool.xfy9326.schedule.ui.vm

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.xfy9326.atools.coroutines.withTryLock
import io.github.xfy9326.atools.livedata.MutableEventLiveData
import io.github.xfy9326.atools.livedata.postEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.beans.BatchResult
import tool.xfy9326.schedule.beans.CourseDetail
import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.WeekNumType
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.io.utils.ImageUtils
import tool.xfy9326.schedule.kt.asDistinctLiveData
import tool.xfy9326.schedule.tools.DisposableValue
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.CalendarUtils
import tool.xfy9326.schedule.utils.ics.ScheduleICSHelper
import tool.xfy9326.schedule.utils.schedule.ScheduleDataProcessor
import tool.xfy9326.schedule.utils.schedule.ScheduleSyncHelper
import tool.xfy9326.schedule.utils.view.ScheduleViewHelper
import java.util.concurrent.atomic.AtomicBoolean

class ScheduleViewModel : AbstractViewModel() {
    val weekNumInfo = ScheduleDataProcessor.weekNumInfoFlow.asDistinctLiveData()
    val scheduleBuildData = ScheduleDataProcessor.scheduleViewDataFlow.asDistinctLiveData()
    val scheduleBackground = ScheduleDataProcessor.scheduleBackgroundFlow.asDistinctLiveData()

    val nowDay = MutableLiveData<Day>()
    val scrollToWeek = MutableEventLiveData<Int>()
    val showWeekChanged = MutableEventLiveData<Pair<Int, WeekNumType>>()
    val showScheduleControlPanel = MutableEventLiveData<Pair<Int, Int>>()
    val showCourseDetailDialog = MutableEventLiveData<CourseDetail>()
    val openCourseManageActivity = MutableEventLiveData<Long>()
    val exitAppDirectly = MutableEventLiveData<Boolean>()
    val toolBarTintColor = ScheduleDataStore.toolBarTintColorFlow.asDistinctLiveData()
    val scheduleSystemBarAppearance = ScheduleDataStore.scheduleSystemBarAppearanceFlow.asDistinctLiveData()

    val scheduleShared = MutableEventLiveData<Uri?>()
    val scheduleImageSaved = MutableEventLiveData<Uri?>()
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
            scrollToWeek.postEvent(ScheduleDataProcessor.weekNumFlow.first())
        }
    }

    fun openCurrentScheduleCourseManageActivity() {
        viewModelScope.launch(Dispatchers.IO) {
            openCourseManageActivity.postEvent(AppDataStore.currentScheduleIdFlow.first())
        }
    }

    fun showScheduleControlPanel() {
        viewModelScope.launch(Dispatchers.IO) {
            showScheduleControlPanel.postEvent(ScheduleDataProcessor.weekNumInfoFlow.first())
        }
    }

    fun notifyShowWeekChanged(weekNum: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            showWeekChanged.postEvent(weekNum to WeekNumType.create(weekNum, ScheduleDataProcessor.weekNumFlow.first()))
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

    fun shareScheduleImage(saveImage: Boolean, weekNum: Int, targetWidth: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            scheduleSharedMutex.withTryLock {
                val scheduleId = AppDataStore.currentScheduleIdFlow.first()
                val waterMark = AppSettingsDataStore.drawWaterMarkOnScheduleImageFlow.first()
                val bitmap = ScheduleViewHelper.generateScheduleImageByWeekNum(scheduleId, weekNum, targetWidth, waterMark)
                if (bitmap != null) {
                    if (saveImage) {
                        val uri = ImageUtils.outputImageToAlbum(bitmap)
                        scheduleImageSaved.postEvent(uri)
                    } else {
                        val uri = ImageUtils.createShareCacheImage(bitmap)
                        scheduleShared.postEvent(uri)
                    }
                } else {
                    scheduleShared.postEvent(null)
                }
            }
        }
    }

    fun showCourseDetailDialog(courseId: Long, timeId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            ScheduleDataProcessor.currentScheduleTimesFlow.combine(ScheduleDBProvider.db.scheduleDAO.getScheduleCourse(courseId)) { times, course ->
                if (course == null) {
                    null
                } else {
                    CourseDetail(timeId, course, times)
                }
            }.firstOrNull()?.let {
                showCourseDetailDialog.postEvent(it)
            }
        }
    }
}