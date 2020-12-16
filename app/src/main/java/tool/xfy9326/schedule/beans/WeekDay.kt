package tool.xfy9326.schedule.beans

import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
enum class WeekDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    companion object {
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
                else -> throw IllegalArgumentException("Calendar week day value error! Must be one of Calendar week day values!")
            }

        fun from(ordinal: Int) = VALUES[ordinal]

        fun of(num: Int): WeekDay {
            if (num !in 1..7) {
                throw IllegalArgumentException("Week day num must in 1..7")
            }
            return VALUES[num - 1]
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