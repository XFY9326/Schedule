package tool.xfy9326.schedule.db.utils

import androidx.room.TypeConverter
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.beans.WeekDay.Companion.value
import tool.xfy9326.schedule.utils.deserializeToBooleanArray
import tool.xfy9326.schedule.utils.serializeToString
import java.util.Date

class DBTypeConverter {
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
        return value?.deserializeToBooleanArray()
    }

    @TypeConverter
    fun booleanArrayToString(booleanArray: BooleanArray?): String? {
        return booleanArray?.serializeToString()
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
    fun stringToScheduleTimeArray(value: String?): List<ScheduleTime>? {
        return value?.let {
            ScheduleTime.deserialize(it)
        }
    }

    @TypeConverter
    fun scheduleTimeArrayToString(arr: List<ScheduleTime>?): String? {
        return arr?.let {
            ScheduleTime.serialize(it)
        }
    }
}