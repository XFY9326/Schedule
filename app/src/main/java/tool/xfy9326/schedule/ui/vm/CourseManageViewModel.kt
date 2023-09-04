package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import io.github.xfy9326.atools.livedata.MutableNotifyLiveData
import io.github.xfy9326.atools.livedata.addAsSource
import io.github.xfy9326.atools.livedata.postNotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.Course.Companion.hasEmptyWeekNumCourseTime
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.asDistinctLiveData

class CourseManageViewModel : AbstractViewModel() {
    private var hasCourseInit: Boolean = false
    var coursesLivaData = MediatorLiveData<List<Course>>()
    val courseRecovered = MutableNotifyLiveData()

    fun requestDBCourses(scheduleId: Long) {
        if (!hasCourseInit) {
            hasCourseInit = true
            ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(scheduleId).map {
                it.sortedWith { a, b ->
                    val aEmptyWeekNum = a.hasEmptyWeekNumCourseTime()
                    val bEmptyWeekNum = b.hasEmptyWeekNumCourseTime()
                    if (aEmptyWeekNum && !bEmptyWeekNum) {
                        -1
                    } else if (!aEmptyWeekNum && bEmptyWeekNum) {
                        1
                    } else {
                        0
                    }
                }
            }.asDistinctLiveData().addAsSource(coursesLivaData)
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