package tool.xfy9326.schedule.utils.schedule

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.provider.CalendarContract
import io.github.xfy9326.atools.base.asArray
import io.github.xfy9326.atools.coroutines.withTryLock
import io.github.xfy9326.atools.io.IOManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.BatchResult
import tool.xfy9326.schedule.beans.CalendarEventDescription
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.Course.Companion.iterateAll
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleCalculateTimes
import tool.xfy9326.schedule.beans.ScheduleSync
import tool.xfy9326.schedule.beans.SectionTime.Companion.description
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.tools.NumberPattern
import tool.xfy9326.schedule.utils.NEW_LINE
import tool.xfy9326.schedule.utils.PROJECT_ID
import tool.xfy9326.schedule.utils.ics.ScheduleICSWriter
import java.util.Date
import java.util.TimeZone

object ScheduleSyncHelper {
    private const val SYNC_ACCOUNT_NAME = PROJECT_ID
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
        contentResolver.delete(CalendarContract.Calendars.CONTENT_URI, CALENDAR_ID_SELECTION, calId.toString().asArray())
    }

    private fun clearAllCalendar(contentResolver: ContentResolver) {
        contentResolver.delete(CalendarContract.Calendars.CONTENT_URI, ALL_CALENDAR_SELECTION, null)
    }

    fun removeAllCalendar() {
        clearAllCalendar(IOManager.contentResolver)
    }

    suspend fun syncCalendar(): BatchResult? = withContext(Dispatchers.Default) {
        syncLock.withTryLock(
            action = {
                val contentResolver = IOManager.contentResolver
                try {
                    clearAllCalendar(contentResolver)

                    val reminderMinutes = AppSettingsDataStore.calendarSyncReminderMinutesFlow.first()
                    val eventDescriptions = AppSettingsDataStore.calendarEventDescriptionsFlow.first()
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
                                if (!syncSchedule(calId, contentResolver, schedule, courses, calIdInfo.second, reminderMinutes, eventDescriptions)) {
                                    errorScheduleAmount++
                                }
                                totalSchedule++
                            }
                        }
                    }

                    return@withTryLock BatchResult(true, totalSchedule, errorScheduleAmount)
                } catch (e: Exception) {
                    clearAllCalendar(contentResolver)
                    e.printStackTrace()
                    return@withTryLock BatchResult(false)
                }
            },
            onHasLocked = { null }
        )
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
            put(
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                if (scheduleSync.editable) CalendarContract.Calendars.CAL_ACCESS_OWNER else CalendarContract.Calendars.CAL_ACCESS_READ
            )
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TIMEZONE_ID)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, SYNC_ACCOUNT_NAME)
            put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0)
        }

    private fun syncSchedule(
        calId: Int,
        contentResolver: ContentResolver,
        schedule: Schedule,
        courses: List<Course>,
        sync: ScheduleSync,
        reminderMinutes: Int,
        eventDescriptions: Set<CalendarEventDescription>
    ): Boolean {
        val scheduleCalculateTimes = ScheduleCalculateTimes(schedule)

        courses.iterateAll { course, courseTime ->
            if (!createCourseTimeCalendarEvent(
                    calId = calId,
                    contentResolver = contentResolver,
                    course = course,
                    courseTime = courseTime,
                    scheduleCalculateTimes = scheduleCalculateTimes,
                    sync = sync,
                    reminderMinutes = reminderMinutes,
                    eventDescriptions = eventDescriptions
                )
            ) {
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
        eventDescriptions: Set<CalendarEventDescription>
    ): Boolean {
        val weekNumPattern = WeekNumPattern.parsePattern(courseTime, scheduleCalculateTimes)
        when (weekNumPattern.type) {
            NumberPattern.PatternType.SINGLE ->
                CourseTimeUtils.getRealSectionTime(scheduleCalculateTimes, weekNumPattern.start + 1, courseTime.sectionTime).let { time ->
                    if (!insertEvent(contentResolver, sync, reminderMinutes, ContentValues().apply {
                            addBasicInfoToCalendarEvent(this, calId, time, course, courseTime, eventDescriptions)
                        })) return false
                }

            NumberPattern.PatternType.SPACED, NumberPattern.PatternType.SERIAL ->
                CourseTimeUtils.getRealSectionTime(scheduleCalculateTimes, weekNumPattern.start + 1, courseTime.sectionTime).let { time ->
                    if (!insertEvent(contentResolver, sync, reminderMinutes, ContentValues().apply {
                            put(
                                CalendarContract.Events.RRULE,
                                ScheduleICSWriter.RRULE(
                                    weekNumPattern.interval,
                                    weekNumPattern.amount,
                                    courseTime.sectionTime.weekDay,
                                    scheduleCalculateTimes.weekStart
                                ).text
                            )
                            addBasicInfoToCalendarEvent(this, calId, time, course, courseTime, eventDescriptions)
                        })) return false
                }

            NumberPattern.PatternType.MESSY ->
                for (period in weekNumPattern.timePeriodArray) {
                    CourseTimeUtils.getRealSectionTime(scheduleCalculateTimes, period.start + 1, courseTime.sectionTime).let { time ->
                        if (!insertEvent(contentResolver, sync, reminderMinutes, ContentValues().apply {
                                if (period.length > 1) {
                                    put(
                                        CalendarContract.Events.RRULE,
                                        ScheduleICSWriter.RRULE(
                                            1,
                                            period.length,
                                            courseTime.sectionTime.weekDay,
                                            scheduleCalculateTimes.weekStart
                                        ).text
                                    )
                                }
                                addBasicInfoToCalendarEvent(this, calId, time, course, courseTime, eventDescriptions)
                            })) return false
                    }
                }

            else -> Unit
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

    private fun addBasicInfoToCalendarEvent(
        values: ContentValues,
        calId: Int,
        time: Pair<Date, Date>,
        course: Course,
        courseTime: CourseTime,
        eventDescriptions: Set<CalendarEventDescription>
    ) {
        values.apply {
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.DTSTART, time.first.time)
            put(CalendarContract.Events.DTEND, time.second.time)
            put(CalendarContract.Events.TITLE, course.name)
            put(CalendarContract.Events.EVENT_TIMEZONE, TIMEZONE_ID)
            put(CalendarContract.Events.EVENT_COLOR, course.color)
            val descriptionLines = mutableListOf<String>()
            if (CalendarEventDescription.SECTIONS in eventDescriptions) {
                descriptionLines.add(IOManager.resources.getString(R.string.ics_description_sections, courseTime.sectionTime.description))
            }
            course.teacher?.takeIf { CalendarEventDescription.TEACHER in eventDescriptions }?.let {
                descriptionLines.add(IOManager.resources.getString(R.string.ics_description_teacher, it))
            }
            courseTime.location?.let {
                put(CalendarContract.Events.EVENT_LOCATION, it)
                if (CalendarEventDescription.LOCATION in eventDescriptions) {
                    descriptionLines.add(IOManager.resources.getString(R.string.ics_description_location, it))
                }
            }
            if (descriptionLines.isNotEmpty()) {
                put(CalendarContract.Events.DESCRIPTION, descriptionLines.joinToString(NEW_LINE))
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