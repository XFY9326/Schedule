package tool.xfy9326.schedule.beans

data class ScheduleBuildBundle(
    val schedule: Schedule,
    val courses: List<Course>,
    val scheduleStyles: ScheduleStyles,
)
