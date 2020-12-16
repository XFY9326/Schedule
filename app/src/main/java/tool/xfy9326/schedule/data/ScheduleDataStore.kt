package tool.xfy9326.schedule.data

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import tool.xfy9326.schedule.beans.ScheduleStyles
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.kt.tryEnumValueOf

object ScheduleDataStore : AbstractDataStore("ScheduleSettings") {
    private val firstDayOfWeek by preferencesKey<String>()
    private val viewAlpha by preferencesKey<Float>()
    private val forceShowWeekendColumn by preferencesKey<Boolean>()
    private val showNotThisWeekCourse by preferencesKey<Boolean>()
    private val timeTextColor by preferencesKey<Int>()
    private val courseCellTextSize by preferencesKey<Float>()
    private val cornerScreenMargin by preferencesKey<Boolean>()

    val firstDayOfWeekFlow = read {
        tryEnumValueOf(it[firstDayOfWeek]) ?: WeekDay.MONDAY
    }

    val scheduleStylesFlow = readOnlyFlow.combine(firstDayOfWeekFlow) { pref, weekday ->
        ScheduleStyles(
            firstDayOfWeek = weekday,
            viewAlpha = pref[viewAlpha] ?: 1f,
            forceShowWeekendColumn = pref[forceShowWeekendColumn] ?: false,
            showNotThisWeekCourse = pref[showNotThisWeekCourse] ?: true,
            timeTextColor = pref[timeTextColor],
            courseCellTextSize = pref[courseCellTextSize],
            cornerScreenMargin = pref[cornerScreenMargin] ?: false
        )
    }.distinctUntilChanged()

    suspend fun setCornerScreenMargin(data: Boolean) = edit {
        it[cornerScreenMargin] = data
    }

    suspend fun setTimeTextColor(@ColorInt data: Int) = edit {
        it[timeTextColor] = data
    }

    suspend fun setCourseCellTextSize(@Px data: Float) = edit {
        it[courseCellTextSize] = data
    }

    suspend fun setViewAlpha(@FloatRange(from = 0.0, to = 1.0) data: Float) = edit {
        it[viewAlpha] = data
    }

    suspend fun setForceShowWeekendColumn(data: Boolean) = edit {
        it[forceShowWeekendColumn] = data
    }

    suspend fun setShowNotThisWeekCourse(data: Boolean) = edit {
        it[showNotThisWeekCourse] = data
    }
}