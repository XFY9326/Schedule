package tool.xfy9326.schedule.beans

import tool.xfy9326.schedule.utils.CalendarUtils
import tool.xfy9326.schedule.utils.CourseTimeUtils

class ScheduleCalculateTimes(schedule: Schedule, val firstDayOfWeek: WeekDay) {
    val times = schedule.times
    val weekCountBeginning = CalendarUtils.getFirstDateInThisWeek(schedule.startDate, firstDayOfWeek)
    val actualStartTime = schedule.startDate
    val actualEndTime = CalendarUtils.getLastTimeOfDay(schedule.endDate)
    val maxWeekNum = CourseTimeUtils.getMaxWeekNum(schedule.startDate, schedule.endDate, firstDayOfWeek)
}