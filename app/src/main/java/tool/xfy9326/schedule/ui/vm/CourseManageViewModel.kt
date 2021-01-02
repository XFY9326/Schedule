package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.MutableNotifyLiveData
import tool.xfy9326.schedule.kt.addAsSource
import tool.xfy9326.schedule.kt.asScopeLiveData
import tool.xfy9326.schedule.kt.notify
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

class CourseManageViewModel : AbstractViewModel() {
    private var hasCourseInit: Boolean = false
    var coursesLivaData = MediatorLiveData<Array<Course>>()
    val courseRecovered = MutableNotifyLiveData()

    fun requestDBCourses(scheduleId: Long) {
        if (!hasCourseInit) {
            hasCourseInit = true
            ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(scheduleId).asScopeLiveData(viewModelScope).addAsSource(coursesLivaData)
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            ScheduleDBProvider.db.scheduleDAO.deleteCourse(course)
        }
    }

    fun recoverCourse(course: Course) {
        viewModelScope.launch {
            ScheduleDBProvider.db.scheduleDAO.putCourse(course.scheduleId, course)
            courseRecovered.notify()
        }
    }
}