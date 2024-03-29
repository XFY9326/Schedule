package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.xfy9326.atools.livedata.MutableEventLiveData
import io.github.xfy9326.atools.livedata.MutableNotifyLiveData
import io.github.xfy9326.atools.livedata.postEvent
import io.github.xfy9326.atools.livedata.postNotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.Course.Companion.clone
import tool.xfy9326.schedule.beans.Course.Companion.hasEmptyWeekNumCourseTime
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.EditError
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.ui.dialog.CourseTimeEditDialog
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.schedule.CourseTimeUtils
import tool.xfy9326.schedule.utils.schedule.CourseUtils

class CourseEditViewModel : AbstractViewModel() {
    private var isEdit = false
    private var originalCourseHashCode: Int? = null
    lateinit var editCourse: Course
        private set

    val courseData = MutableLiveData<Course>()
    val courseSaveFailed = MutableEventLiveData<EditError>()
    val courseSaveComplete = MutableEventLiveData<Long>()
    val courseSaveEmptyWeekNum = MutableNotifyLiveData()
    val editCourseTime = MutableEventLiveData<CourseTimeEditDialog.EditBundle>()
    val loadAllSchedules = MutableEventLiveData<List<Schedule.Min>>()
    val copyToOtherSchedule = MutableEventLiveData<EditError?>()

    fun requestDBCourse(scheduleId: Long, courseId: Long) {
        isEdit = courseId != 0L

        if (!::editCourse.isInitialized) {
            viewModelScope.launch(Dispatchers.IO) {
                if (isEdit) {
                    ScheduleDBProvider.db.scheduleDAO.getScheduleCourse(courseId).firstOrNull()?.let {
                        editCourse = it
                        courseData.postValue(it)
                    }
                } else {
                    editCourse = CourseUtils.createNewCourse(scheduleId)
                    courseData.postValue(editCourse)
                }
                originalCourseHashCode = editCourse.hashCode()
            }
        } else {
            courseData.value = editCourse
        }
    }

    fun editCourseTime(scheduleId: Long, courseTime: CourseTime? = null, position: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            ScheduleDBProvider.db.scheduleDAO.getSchedule(scheduleId).firstOrNull()?.let {
                editCourseTime.postEvent(
                    // Deep copy course time
                    CourseTimeEditDialog.EditBundle(
                        CourseTimeUtils.getMaxWeekNum(it.startDate, it.endDate, it.weekStart),
                        it.times.size,
                        courseTime?.copy(sectionTime = courseTime.sectionTime.copy()),
                        position
                    )
                )
            }
        }
    }

    fun loadAllSchedules(currentScheduleId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            loadAllSchedules.postEvent(ScheduleDBProvider.db.scheduleDAO.getScheduleMin().first().filter {
                it.scheduleId != currentScheduleId
            })
        }
    }

    fun copyToOtherSchedule(scheduleId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val cache = editCourse.clone(scheduleId)
            val otherCourses = ScheduleDBProvider.db.scheduleDAO.getScheduleCoursesWithoutId(scheduleId, cache.courseId).first()
            val errorMsg = CourseUtils.validateCourse(cache, otherCourses)
            if (errorMsg == null) {
                ScheduleDBProvider.db.scheduleDAO.putCourse(scheduleId, cache)
                copyToOtherSchedule.postEvent(null)
            } else {
                copyToOtherSchedule.postEvent(errorMsg)
            }
        }
    }

    fun hasDataChanged() = originalCourseHashCode != editCourse.hashCode()

    fun saveCourse(scheduleId: Long, checkEmptyWeekNum: Boolean = true) {
        val cache = editCourse
        if (checkEmptyWeekNum && cache.hasEmptyWeekNumCourseTime()) {
            courseSaveEmptyWeekNum.postNotify()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val otherCourses = ScheduleDBProvider.db.scheduleDAO.getScheduleCoursesWithoutId(scheduleId, cache.courseId).first()
                val errorMsg = CourseUtils.validateCourse(cache, otherCourses)
                if (errorMsg == null) {
                    val newId = if (isEdit) {
                        ScheduleDBProvider.db.scheduleDAO.updateCourse(cache)
                        cache.courseId
                    } else {
                        isEdit = true
                        ScheduleDBProvider.db.scheduleDAO.putCourse(scheduleId, cache).also {
                            editCourse.courseId = it
                        }
                    }
                    originalCourseHashCode = editCourse.hashCode()
                    courseSaveComplete.postEvent(newId)
                } else {
                    courseSaveFailed.postEvent(errorMsg)
                }
            }
        }
    }
}