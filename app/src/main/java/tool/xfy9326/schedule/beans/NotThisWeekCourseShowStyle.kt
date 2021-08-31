package tool.xfy9326.schedule.beans

enum class NotThisWeekCourseShowStyle {
    SHOW_NOT_THIS_WEEK_TEXT,
    USE_TRANSPARENT_BACKGROUND;

    companion object {
        val valueSet by lazy { values().toSet() }
    }
}