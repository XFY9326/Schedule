package tool.xfy9326.schedule.beans

class ScheduleImportRequestParams<I>(val isCurrentSchedule: Boolean, val importParams: I, val importOption: Int = 0)