@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.beans

import android.os.Parcelable
import androidx.annotation.IntegerRes
import androidx.datastore.preferences.core.Preferences
import kotlinx.parcelize.Parcelize
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.annotation.Sp
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.io.IOManager

enum class ScheduleText(val prefKey: Preferences.Key<Int>, @IntegerRes private val defaultTextSize: Int) {
    COURSE_TEXT(ScheduleDataStore.courseTextSize, R.integer.schedule_course_default_text_size),
    SCHEDULE_TIME_TEXT(ScheduleDataStore.scheduleTimeTextSize, R.integer.schedule_time_default_text_size),
    SCHEDULE_NUMBER_TEXT(ScheduleDataStore.scheduleNumberTextSize, R.integer.schedule_number_default_text_size),
    HEADER_MONTH_TEXT(ScheduleDataStore.headerMonthTextSize, R.integer.schedule_header_month_default_text_size),
    HEADER_MONTH_DATE_TEXT(ScheduleDataStore.headerMonthDateTextSize, R.integer.schedule_header_month_date_default_text_size),
    HEADER_WEEKDAY_TEXT(ScheduleDataStore.headerWeekDayTextSize, R.integer.schedule_header_weekday_default_text_size);

    @Parcelize
    class TextSize private constructor(private val sizeMap: Map<ScheduleText, Int>) : Parcelable {
        companion object {
            @get:Sp
            val maxSize
                get() = IOManager.resources.getInteger(R.integer.schedule_text_size_max)

            @get:Sp
            val minSize
                get() = IOManager.resources.getInteger(R.integer.schedule_text_size_min)

            @get:Sp
            val sizeOffset
                get() = IOManager.resources.getInteger(R.integer.schedule_text_size_offset)

            fun ScheduleText.getTextSize(pref: Preferences) = (pref[prefKey] ?: IOManager.resources.getInteger(defaultTextSize))

            fun create(pref: Preferences, textType: ScheduleText? = null): TextSize {
                return if (textType == null) {
                    val values = values()
                    val sizeMap = HashMap<ScheduleText, Int>(values.size).apply {
                        for (value in values) {
                            this[value] = value.getTextSize(pref)
                        }
                    }
                    TextSize(sizeMap)
                } else {
                    TextSize(mapOf(textType to textType.getTextSize(pref)))
                }
            }
        }

        operator fun get(textType: ScheduleText) = (getRaw(textType) + sizeOffset).toFloat()

        fun getRaw(textType: ScheduleText) =
            sizeMap[textType] ?: throw NoSuchElementException("This text type can't be found! May be you specific text type when create? Text type: $textType")
    }
}