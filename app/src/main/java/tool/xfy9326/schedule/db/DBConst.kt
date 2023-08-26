package tool.xfy9326.schedule.db

object DBConst {
    const val DEFAULT_ID = 0L

    const val TABLE_SCHEDULE = "Schedule"
    const val TABLE_COURSE = "Course"
    const val TABLE_COURSE_TIME = "CourseTime"
    const val TABLE_SCHEDULE_SYNC = "ScheduleSync"

    const val COLUMN_SCHEDULE_ID = "scheduleId"
    const val COLUMN_SCHEDULE_NAME = "name"
    const val COLUMN_SCHEDULE_TIMES = "times"
    const val COLUMN_COURSE_ID = "courseId"
    const val COLUMN_TIME_ID = "timeId"
    const val COLUMN_WEEK_DAY = "weekDay"
    const val COLUMN_SECTION_START = "classStartTime"
    const val COLUMN_SECTION_DURATION = "classDuration"
    const val COLUMN_WEEK_NUM = "weekNum"
    const val COLUMN_LOCATION = "location"
    const val COLUMN_SYNC_ID = "syncId"

    const val LIKE_MORE = "%"
    const val LIKE_SINGLE = "_"
}