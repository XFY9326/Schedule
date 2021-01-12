package tool.xfy9326.schedule.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.iterateAll
import java.util.*

object ScheduleSyncHelper {
    private const val SYNC_ACCOUNT_NAME = "Schedule"
    private const val SYNC_ACCOUNT_TYPE = CalendarContract.ACCOUNT_TYPE_LOCAL

    private const val CALENDAR_ID_SELECTION = "${CalendarContract.Calendars._ID}=?"
    private const val ALL_CALENDAR_SELECTION =
        "${CalendarContract.Calendars.ACCOUNT_NAME}='$SYNC_ACCOUNT_NAME' and ${CalendarContract.Calendars.ACCOUNT_TYPE}='$SYNC_ACCOUNT_TYPE'"

    private val CALENDAR_ACCOUNT_EDIT_URI = CalendarContract.Calendars.CONTENT_URI.buildUpon()
        .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, true.toString())
        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, SYNC_ACCOUNT_NAME)
        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, SYNC_ACCOUNT_TYPE)
        .build()

    private val TIMEZONE_ID = TimeZone.getDefault().id
    private val syncLock = Mutex()

    private fun clearCalendar(calId: Int, contentResolver: ContentResolver) {
        contentResolver.delete(CalendarContract.Calendars.CONTENT_URI, CALENDAR_ID_SELECTION, arrayOf(calId.toString()))
    }

    private fun clearAllCalendar(contentResolver: ContentResolver) {
        contentResolver.delete(CalendarContract.Calendars.CONTENT_URI, ALL_CALENDAR_SELECTION, null)
    }

    fun removeAllCalendar(context: Context) {
        clearAllCalendar(context.contentResolver)
    }

    private fun getWeeklyRRULEText(interval: Int, count: Int, weekDay: WeekDay, firstDayOfWeek: WeekDay) =
        "FREQ=WEEKLY;COUNT=$count;INTERVAL=$interval;BYDAY=${getDayOfWeek(weekDay)};WKST=${getDayOfWeek(firstDayOfWeek)}"

    private fun getDayOfWeek(weekDay: WeekDay) =
        when (weekDay) {
            WeekDay.MONDAY -> "MO"
            WeekDay.TUESDAY -> "TU"
            WeekDay.WEDNESDAY -> "WE"
            WeekDay.THURSDAY -> "TH"
            WeekDay.FRIDAY -> "FR"
            WeekDay.SATURDAY -> "SA"
            WeekDay.SUNDAY -> "SU"
        }

    suspend fun syncCalendar(context: Context) = withContext(Dispatchers.Unconfined) {
        val contentResolver = context.contentResolver
        if (syncLock.tryLock()) {
            try {
                clearAllCalendar(contentResolver)

                val firstDayOfWeek = ScheduleDataStore.firstDayOfWeekFlow.first()
                val reminderMinutes = AppSettingsDataStore.calendarSyncReminderMinutesFlow.first()
                val schedules = ScheduleDBProvider.db.scheduleDAO.getSchedules().first()
                var totalSchedule = 0
                var errorScheduleAmount = 0
                for (schedule in schedules) {
                    val calIdInfo = getCalendarId(contentResolver, schedule)

                    if (calIdInfo.second.syncable) {
                        val calId = calIdInfo.first
                        if (calId == null) {
                            errorScheduleAmount++
                            totalSchedule++
                        } else {
                            val courses = ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(schedule.scheduleId).first()
                            if (!syncSchedule(calId, contentResolver, firstDayOfWeek, schedule, courses, calIdInfo.second, reminderMinutes)) {
                                errorScheduleAmount++
                            }
                            totalSchedule++
                        }
                    }
                }

                return@withContext ScheduleSync.Result(true, totalSchedule, errorScheduleAmount)
            } catch (e: Exception) {
                clearAllCalendar(contentResolver)
                e.printStackTrace()
                return@withContext ScheduleSync.Result(false)
            } finally {
                syncLock.unlock()
            }
        } else {
            return@withContext null
        }
    }

    private suspend fun getCalendarId(contentResolver: ContentResolver, schedule: Schedule): Pair<Int?, ScheduleSync> {
        val scheduleSync = ScheduleDBProvider.db.scheduleSyncDao.getScheduleSync(schedule.scheduleId).first()

        return if (scheduleSync.syncable) {
            val contentValues = getSyncCalendarValue(schedule, scheduleSync)
            val resultUri = contentResolver.insert(CALENDAR_ACCOUNT_EDIT_URI, contentValues)
            if (resultUri != null) {
                ContentUris.parseId(resultUri).toInt() to scheduleSync
            } else {
                null to scheduleSync
            }
        } else {
            null to scheduleSync
        }
    }

    private fun getSyncCalendarValue(schedule: Schedule, scheduleSync: ScheduleSync) =
        ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, SYNC_ACCOUNT_NAME)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, SYNC_ACCOUNT_TYPE)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, schedule.name)
            put(CalendarContract.Calendars.NAME, "$SYNC_ACCOUNT_NAME-${schedule.name}")
            put(CalendarContract.Calendars.CALENDAR_COLOR, schedule.color)
            put(CalendarContract.Calendars.VISIBLE, if (scheduleSync.defaultVisible) 1 else 0)
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                if (scheduleSync.editable) CalendarContract.Calendars.CAL_ACCESS_OWNER else CalendarContract.Calendars.CAL_ACCESS_READ)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TIMEZONE_ID)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, SYNC_ACCOUNT_NAME)
            put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0)
        }

    private fun syncSchedule(
        calId: Int,
        contentResolver: ContentResolver,
        firstDayOfWeek: WeekDay,
        schedule: Schedule,
        courses: Array<Course>,
        sync: ScheduleSync,
        reminderMinutes: Int,
    ): Boolean {
        val scheduleCalculateTimes = ScheduleCalculateTimes(schedule, firstDayOfWeek)

        courses.iterateAll { course, courseTime ->
            if (!createCourseTimeCalendarEvent(calId, contentResolver, course, courseTime, scheduleCalculateTimes, sync, reminderMinutes)) {
                clearCalendar(calId, contentResolver)
                return false
            }
        }
        return true
    }

    private fun createCourseTimeCalendarEvent(
        calId: Int,
        contentResolver: ContentResolver,
        course: Course,
        courseTime: CourseTime,
        scheduleCalculateTimes: ScheduleCalculateTimes,
        sync: ScheduleSync,
        reminderMinutes: Int,
    ): Boolean {
        val weekNumPattern = WeekNumPattern(courseTime, scheduleCalculateTimes)
        if (weekNumPattern.type == WeekNumPattern.PatternType.SINGLE) {
            CourseTimeUtils.getRealClassTime(scheduleCalculateTimes, weekNumPattern.start + 1, courseTime.classTime).let { time ->
                if (!insertEvent(contentResolver, sync, reminderMinutes, ContentValues().apply {
                        addBasicInfoToCalendarEvent(this, calId, time, course, courseTime)
                    })) return false
            }
        } else if (weekNumPattern.type == WeekNumPattern.PatternType.SPACED || weekNumPattern.type == WeekNumPattern.PatternType.SERIAL) {
            CourseTimeUtils.getRealClassTime(scheduleCalculateTimes, weekNumPattern.start + 1, courseTime.classTime).let { time ->
                if (!insertEvent(contentResolver, sync, reminderMinutes, ContentValues().apply {
                        put(
                            CalendarContract.Events.RRULE,
                            getWeeklyRRULEText(weekNumPattern.interval,
                                weekNumPattern.amount,
                                courseTime.classTime.weekDay,
                                scheduleCalculateTimes.firstDayOfWeek)
                        )
                        addBasicInfoToCalendarEvent(this, calId, time, course, courseTime)
                    })) return false
            }
        } else if (weekNumPattern.type == WeekNumPattern.PatternType.MESSY) {
            for (period in weekNumPattern.timePeriodArray) {
                CourseTimeUtils.getRealClassTime(scheduleCalculateTimes, period.start + 1, courseTime.classTime).let { time ->
                    if (!insertEvent(contentResolver, sync, reminderMinutes, ContentValues().apply {
                            if (period.length > 1) {
                                put(
                                    CalendarContract.Events.RRULE,
                                    getWeeklyRRULEText(1, period.length, courseTime.classTime.weekDay, scheduleCalculateTimes.firstDayOfWeek)
                                )
                            }
                            addBasicInfoToCalendarEvent(this, calId, time, course, courseTime)
                        })) return false
                }
            }
        }
        return true
    }

    private fun insertEvent(contentResolver: ContentResolver, sync: ScheduleSync, reminderMinutes: Int, values: ContentValues): Boolean {
        val resultUri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        if (resultUri != null) {
            return if (sync.addReminder) {
                val eventId = ContentUris.parseId(resultUri).toInt()
                addReminder(eventId, contentResolver, reminderMinutes)
            } else {
                true
            }
        }
        return false
    }

    private fun addBasicInfoToCalendarEvent(values: ContentValues, calId: Int, time: Pair<Date, Date>, course: Course, courseTime: CourseTime) {
        values.apply {
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.DTSTART, time.first.time)
            put(CalendarContract.Events.DTEND, time.second.time)
            put(CalendarContract.Events.TITLE, course.name)
            put(CalendarContract.Events.EVENT_TIMEZONE, TIMEZONE_ID)
            put(CalendarContract.Events.EVENT_COLOR, course.color)
            courseTime.location?.let {
                put(CalendarContract.Events.EVENT_LOCATION, App.instance.getString(R.string.ics_description_teacher, it))
            }
            course.teacher?.let {
                put(CalendarContract.Events.DESCRIPTION, it)
            }
        }
    }

    private fun addReminder(eventId: Int, contentResolver: ContentResolver, reminderMinutes: Int): Boolean {
        val values = ContentValues().apply {
            put(CalendarContract.Reminders.MINUTES, reminderMinutes)
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        return contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values) != null
    }
}