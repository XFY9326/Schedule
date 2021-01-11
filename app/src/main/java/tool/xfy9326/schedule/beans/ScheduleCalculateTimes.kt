package tool.xfy9326.schedule.beans

import tool.xfy9326.schedule.utils.CalendarUtils

class ScheduleCalculateTimes(schedule: Schedule, val firstDayOfWeek: WeekDay) {
    val times = schedule.times
    val weekCountBeginning = CalendarUtils.getFirstDateInThisWeek(schedule.startDate, firstDayOfWeek)
    val actualStartTime = schedule.startDate
    val actualEndTime = CalendarUtils.getLastTimeOfDay(schedule.endDate)
}