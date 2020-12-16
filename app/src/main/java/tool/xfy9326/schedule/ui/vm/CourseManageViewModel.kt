package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.MutableNotifyLiveData
import tool.xfy9326.schedule.kt.asScopeLiveData
import tool.xfy9326.schedule.kt.notify
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

class CourseManageViewModel : AbstractViewModel() {
    private var coursesLivaData: LiveData<Array<Course>>? = null
    val courseRecovered = MutableNotifyLiveData()

    fun requestDBCourses(scheduleId: Long) =
        if (coursesLivaData == null) {
            ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(scheduleId).asScopeLiveData(viewModelScope).also {
                coursesLivaData = it
            }
        } else {
            coursesLivaData!!
        }

    fun deleteCourse(course: Course) {
        viewModelScope.launch(Dispatchers.IO) {
            ScheduleDBProvider.db.scheduleDAO.deleteCourse(course)
        }
    }

    fun recoverCourse(course: Course) {
        viewModelScope.launch(Dispatchers.IO) {
            ScheduleDBProvider.db.scheduleDAO.putCourse(course.scheduleId, course)
            courseRecovered.notify()
        }
    }
}