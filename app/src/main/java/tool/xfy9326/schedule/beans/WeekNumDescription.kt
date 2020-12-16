package tool.xfy9326.schedule.beans

import android.content.Context
import tool.xfy9326.schedule.R

class WeekNumDescription(
    private val content: String,
    private val weekMode: WeekMode,
) {

    fun getText(context: Context) =
        if (weekMode == WeekMode.ANY_WEEKS) content
        else context.getString(if (weekMode == WeekMode.ODD_WEEKS_ONLY) R.string.odd_week_description else R.string.even_week_description, content)
}