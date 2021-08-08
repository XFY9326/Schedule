package tool.xfy9326.schedule.utils.view

import tool.xfy9326.schedule.utils.schedule.NextCourseUtils
import tool.xfy9326.schedule.utils.schedule.ScheduleDataProcessor

object NextCourseWidgetUtils {
    fun initDataObserver() {
        ScheduleDataProcessor.addCurrentScheduleCourseDataGlobalListener { schedule ->
            val nextCourse = NextCourseUtils.getNextCourseByDate(schedule)
        }
    }
}