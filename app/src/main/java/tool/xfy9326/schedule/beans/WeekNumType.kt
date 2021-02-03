package tool.xfy9326.schedule.beans

import android.content.Context
import androidx.annotation.StringRes
import tool.xfy9326.schedule.R

enum class WeekNumType(@StringRes private val textId: Int) {
    NOT_CURRENT_WEEK(R.string.not_current_week),
    CURRENT_WEEK(R.string.current_week),
    NEXT_WEEK(R.string.next_week),
    IN_VACATION(R.string.in_vacation);

    companion object {
        fun create(showWeekNum: Int, nowWeekNum: Int) =
            when {
                nowWeekNum == 0 -> IN_VACATION
                nowWeekNum == showWeekNum -> CURRENT_WEEK
                nowWeekNum + 1 == showWeekNum -> NEXT_WEEK
                else -> NOT_CURRENT_WEEK
            }
    }

    fun getText(context: Context) = context.getString(textId)
}