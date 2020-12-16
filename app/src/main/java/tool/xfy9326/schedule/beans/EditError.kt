@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.beans

import android.content.Context
import tool.xfy9326.schedule.R

class EditError private constructor(
    val errorType: Type,
    vararg errorTextArgs: Any,
) {
    private val textArgs = errorTextArgs

    enum class Type {
        SCHEDULE_NAME_EMPTY,
        SCHEDULE_DATE_ERROR,
        SCHEDULE_MAX_WEEK_NUM_ERROR,
        SCHEDULE_TIME_CONFLICT_ERROR,
        SCHEDULE_COURSE_NUM_ERROR,
        SCHEDULE_COURSE_WEEK_NUM_ERROR,
        COURSE_TIME_LIST_EMPTY,
        COURSE_NAME_EMPTY,
        COURSE_TIME_INNER_CONFLICT_ERROR,
        COURSE_TIME_OTHERS_CONFLICT_ERROR;

        fun make(vararg textArgs: Any) = EditError(this, *textArgs)
    }

    fun getText(context: Context) =
        context.getString(
            when (errorType) {
                Type.SCHEDULE_NAME_EMPTY -> R.string.schedule_name_empty_error
                Type.SCHEDULE_DATE_ERROR -> R.string.start_date_larger_than_end_date_error
                Type.SCHEDULE_MAX_WEEK_NUM_ERROR -> R.string.max_week_num_error
                Type.SCHEDULE_TIME_CONFLICT_ERROR -> R.string.schedule_time_conflict_error
                Type.SCHEDULE_COURSE_NUM_ERROR -> R.string.course_num_error
                Type.SCHEDULE_COURSE_WEEK_NUM_ERROR -> R.string.course_week_num_error
                Type.COURSE_TIME_LIST_EMPTY -> R.string.course_time_empty
                Type.COURSE_NAME_EMPTY -> R.string.course_name_empty
                Type.COURSE_TIME_INNER_CONFLICT_ERROR -> R.string.course_time_inner_conflict
                Type.COURSE_TIME_OTHERS_CONFLICT_ERROR -> R.string.course_time_others_conflict
            }, *textArgs
        )
}