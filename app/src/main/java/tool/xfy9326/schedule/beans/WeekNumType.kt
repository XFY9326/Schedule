package tool.xfy9326.schedule.beans

import android.content.Context
import tool.xfy9326.schedule.R

enum class WeekNumType {
    NOT_CURRENT_WEEK,
    CURRENT_WEEK,
    NEXT_WEEK,
    IN_VACATION;

    companion object {
        fun create(showWeekNum: Int, nowWeekNum: Int) =
            when {
                nowWeekNum == 0 -> IN_VACATION
                nowWeekNum == showWeekNum -> CURRENT_WEEK
                nowWeekNum + 1 == showWeekNum -> NEXT_WEEK
                else -> NOT_CURRENT_WEEK
            }

        fun WeekNumType.getText(context: Context) =
            context.getString(
                when (this) {
                    NOT_CURRENT_WEEK -> R.string.not_current_week
                    CURRENT_WEEK -> R.string.current_week
                    NEXT_WEEK -> R.string.next_week
                    IN_VACATION -> R.string.in_vacation
                }
            )
    }
}