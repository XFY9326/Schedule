package tool.xfy9326.schedule.db.utils

import androidx.room.TypeConverter
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.WeekDay
import java.util.*

class DBTypeConverter {
    companion object {
        val instance by lazy {
            DBTypeConverter()
        }
    }

    @TypeConverter
    fun timestampToDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun stringToBooleanArray(value: String?): BooleanArray? {
        return value?.let {
            BooleanArray(it.length) { p ->
                value[p] == '1'
            }
        }
    }

    @TypeConverter
    fun booleanArrayToString(booleanArray: BooleanArray?): String? {
        return booleanArray?.let {
            buildString(it.size) {
                it.forEach { b ->
                    append(if (b) '1' else '0')
                }
            }
        }
    }

    @TypeConverter
    fun intToWeekDay(value: Int?): WeekDay? {
        return value?.let { WeekDay.of(it) }
    }

    @TypeConverter
    fun weekDayToInt(weekDay: WeekDay?): Int? {
        return weekDay?.value
    }

    @TypeConverter
    fun stringToScheduleTimeArray(value: String?): Array<ScheduleTime>? {
        return value?.let {
            ScheduleTime.deserialize(it)
        }
    }

    @TypeConverter
    fun scheduleTimeArrayToString(arr: Array<ScheduleTime>?): String? {
        return arr?.let {
            ScheduleTime.serialize(it)
        }
    }
}