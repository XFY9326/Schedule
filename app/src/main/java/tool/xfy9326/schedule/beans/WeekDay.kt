package tool.xfy9326.schedule.beans

import java.util.*

@Suppress("unused")
enum class WeekDay {
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

        private val VALUES = values()
        const val MAX_VALUE = 7
        const val MIN_VALUE = 1

        fun fromCalWeekDay(calWeekDay: Int) =
            when (calWeekDay) {
                Calendar.MONDAY -> MONDAY
                Calendar.TUESDAY -> TUESDAY
                Calendar.WEDNESDAY -> WEDNESDAY
                Calendar.THURSDAY -> THURSDAY
                Calendar.FRIDAY -> FRIDAY
                Calendar.SATURDAY -> SATURDAY
                Calendar.SUNDAY -> SUNDAY
                else -> error("Calendar week day value error! Must be one of Calendar week day values!")
            }

        fun from(ordinal: Int) = VALUES[ordinal]

        fun of(num: Int): WeekDay {
            if (num !in 1..7) {
                error("Week day num must in 1..7")
            }
            return VALUES[num - 1]
        }

        fun valueOfShortName(str: String) =
            when (str.toUpperCase(Locale.getDefault())) {
                SHORT_NAME_MO -> MONDAY
                SHORT_NAME_TU -> TUESDAY
                SHORT_NAME_WE -> WEDNESDAY
                SHORT_NAME_TH -> THURSDAY
                SHORT_NAME_FR -> FRIDAY
                SHORT_NAME_SA -> SATURDAY
                SHORT_NAME_SU -> SUNDAY
                else -> error("Value error!")
            }

        private fun getShortName(weekDay: WeekDay) =
            when (weekDay) {
                MONDAY -> SHORT_NAME_MO
                TUESDAY -> SHORT_NAME_TU
                WEDNESDAY -> SHORT_NAME_WE
                THURSDAY -> SHORT_NAME_TH
                FRIDAY -> SHORT_NAME_FR
                SATURDAY -> SHORT_NAME_SA
                SUNDAY -> SHORT_NAME_SU
            }
    }

    val calWeekDay: Int
        get() {
            return when (this) {
                MONDAY -> Calendar.MONDAY
                TUESDAY -> Calendar.TUESDAY
                WEDNESDAY -> Calendar.WEDNESDAY
                THURSDAY -> Calendar.THURSDAY
                FRIDAY -> Calendar.FRIDAY
                SATURDAY -> Calendar.SATURDAY
                SUNDAY -> Calendar.SUNDAY
            }
        }

    val value: Int = ordinal + 1

    val isWeekend: Boolean
        get() = (this == SATURDAY || this == SUNDAY)

    val shortName: String
        get() = getShortName(this)

    fun value(firstDayOfWeek: WeekDay): Int {
        return when (firstDayOfWeek) {
            MONDAY -> value
            SUNDAY ->
                if (this == SUNDAY) {
                    1
                } else {
                    value + 1
                }
            else -> throw IllegalArgumentException("First day of week must be MONDAY or SUNDAY")
        }
    }
}