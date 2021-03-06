package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lib.xfy9326.livedata.MutableNotifyLiveData
import lib.xfy9326.livedata.addAsSource
import lib.xfy9326.livedata.postNotify
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.asDistinctLiveData
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

class CourseManageViewModel : AbstractViewModel() {
    private var hasCourseInit: Boolean = false
    var coursesLivaData = MediatorLiveData<List<Course>>()
    val courseRecovered = MutableNotifyLiveData()

    fun requestDBCourses(scheduleId: Long) {
        if (!hasCourseInit) {
            hasCourseInit = true
            ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(scheduleId).asDistinctLiveData().addAsSource(coursesLivaData)
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch(Dispatchers.IO) {
            ScheduleDBProvider.db.scheduleDAO.deleteCourse(course)
        }
    }

    fun recoverCourse(course: Course) {
        viewModelScope.launch(Dispatchers.IO) {
            ScheduleDBProvider.db.scheduleDAO.putCourse(course.scheduleId, course)
            courseRecovered.postNotify()
        }
    }
}