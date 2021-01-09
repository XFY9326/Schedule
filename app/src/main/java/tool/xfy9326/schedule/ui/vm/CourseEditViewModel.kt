package tool.xfy9326.schedule.ui.vm

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.EditError
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.ui.dialog.CourseTimeEditDialog
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.CourseManager

class CourseEditViewModel : AbstractViewModel() {
    var isEdit = false
        private set
    private var originalCourseHashCode: Int? = null
    lateinit var editCourse: Course
        private set

    val courseData = MutableLiveData<Course>()
    val courseSaveFailed = MutableEventLiveData<EditError>()
    val courseSaveComplete = MutableEventLiveData<Long>()
    val editCourseTime = MutableEventLiveData<CourseTimeEditDialog.EditBundle>()
    val loadAllSchedules = MutableEventLiveData<List<Schedule.Min>>()
    val copyToOtherSchedule = MutableEventLiveData<EditError?>()

    fun requestDBCourse(context: Context, scheduleId: Long, courseId: Long) {
        val weakContext = context.weak()
        isEdit = courseId != 0L

        if (!::editCourse.isInitialized) {
            viewModelScope.launch {
                if (isEdit) {
                    ScheduleDBProvider.db.scheduleDAO.getScheduleCourse(courseId).firstOrNull()?.let {
                        editCourse = it
                        courseData.postValue(it)
                    }
                } else {
                    weakContext.get()?.let {
                        editCourse = CourseManager.createNewCourse(it, scheduleId)
                        courseData.postValue(editCourse)
                    }
                }
                originalCourseHashCode = editCourse.hashCode()
            }
        } else {
            courseData.value = editCourse
        }
    }

    fun editCourseTime(scheduleId: Long, courseTime: CourseTime? = null, position: Int? = null) {
        viewModelScope.launch {
            ScheduleDBProvider.db.scheduleDAO.getSchedule(scheduleId).firstOrNull()?.let {
                editCourseTime.postEvent(
                    // Deep copy course time
                    CourseTimeEditDialog.EditBundle(
                        it.maxWeekNum,
                        it.times.size,
                        courseTime?.copy(classTime = courseTime.classTime.copy()),
                        position
                    )
                )
            }
        }
    }

    fun loadAllSchedules(currentScheduleId: Long) {
        viewModelScope.launch {
            loadAllSchedules.postEvent(ScheduleDBProvider.db.scheduleDAO.getScheduleMin().first().filter {
                it.scheduleId != currentScheduleId
            })
        }
    }

    fun copyToOtherSchedule(scheduleId: Long) {
        viewModelScope.launch {
            val cache = editCourse.clone(scheduleId)
            val errorMsg = validateCourse(scheduleId, cache)
            if (errorMsg == null) {
                ScheduleDBProvider.db.scheduleDAO.putCourse(scheduleId, cache)
                copyToOtherSchedule.postEvent(null)
            } else {
                copyToOtherSchedule.postEvent(errorMsg)
            }
        }
    }

    fun hasDataChanged() = originalCourseHashCode != editCourse.hashCode()

    fun saveCourse(scheduleId: Long) {
        val cache = editCourse
        viewModelScope.launch {
            val errorMsg = validateCourse(scheduleId, cache)
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

    private suspend fun validateCourse(scheduleId: Long, course: Course): EditError? {
        if (course.name.isBlank() || course.name.isEmpty()) {
            return EditError.Type.COURSE_NAME_EMPTY.make()
        }

        if (course.times.isEmpty()) {
            EditError.Type.COURSE_TIME_LIST_EMPTY.make()
        }

        course.times.forEachTwo { i1, courseTime1, i2, courseTime2 ->
            if (courseTime1 intersect courseTime2) return EditError.Type.COURSE_TIME_INNER_CONFLICT_ERROR.make(i1 + 1, i2 + 1)
        }

        val otherCourses = ScheduleDBProvider.db.scheduleDAO.getScheduleCoursesWithoutId(scheduleId, course.courseId).first()

        for (others in otherCourses) {
            for (time in others.times) {
                for ((i, courseTime) in course.times.withIndex()) {
                    if (courseTime intersect time) {
                        return EditError.Type.COURSE_TIME_OTHERS_CONFLICT_ERROR.make(i + 1, others.name)
                    }
                }
            }
        }

        return null
    }
}