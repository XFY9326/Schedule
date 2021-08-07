@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.beans

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
enum class WeekDay : Parcelable {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    companion object {
        private const val SHORT_NAME_MO = "MO"
        private const val SHORT_NAME_TU = "TU"
        private const val SHORT_NAME_WE = "WE"
        private const val SHORT_NAME_TH = "TH"
        private const val SHORT_NAME_FR = "FR"
        private const val SHORT_NAME_SA = "SA"
        private const val SHORT_NAME_SU = "SU"

        const val MAX_VALUE = 7
        const val MIN_VALUE = 1

        val WeekDay.shortName
            get() = when (this) {
                MONDAY -> SHORT_NAME_MO
                TUESDAY -> SHORT_NAME_TU
                WEDNESDAY -> SHORT_NAME_WE
                THURSDAY -> SHORT_NAME_TH
                FRIDAY -> SHORT_NAME_FR
                SATURDAY -> SHORT_NAME_SA
                SUNDAY -> SHORT_NAME_SU
            }

        val WeekDay.calWeekDay
            get() = when (this) {
                MONDAY -> Calendar.MONDAY
                TUESDAY -> Calendar.TUESDAY
                WEDNESDAY -> Calendar.WEDNESDAY
                THURSDAY -> Calendar.THURSDAY
                FRIDAY -> Calendar.FRIDAY
                SATURDAY -> Calendar.SATURDAY
                SUNDAY -> Calendar.SUNDAY
            }

        fun Calendar.getWeekDay() = valueOfCalWeekDay(get(Calendar.DAY_OF_WEEK))

        fun valueOfCalWeekDay(calWeekDay: Int) =
            values().find {
                it.calWeekDay == calWeekDay
            } ?: error("Calendar week day value error! Must be one of Calendar week day values!")

        fun from(ordinal: Int) = values()[ordinal]

        fun of(value: Int): WeekDay {
            if (value !in 1..7) {
                error("Week day num must in 1..7")
            }
            return values()[value - 1]
        }

        fun valueOfShortName(str: String) =
            str.uppercase(Locale.getDefault()).let { shortName ->
                values().find {
                    it.shortName == shortName
                } ?: error("Value error!")
            }

        fun WeekDay.orderedValue(weekStart: WeekDay) =
            when (weekStart) {
                MONDAY -> value
                SUNDAY -> if (this == SUNDAY) 1 else value + 1
                else -> error("First day of week must be MONDAY or SUNDAY")
            }

        val WeekDay.value
            get() = ordinal + 1

        val WeekDay.isWeekend
            get() = (this == SATURDAY || this == SUNDAY)
    }
}