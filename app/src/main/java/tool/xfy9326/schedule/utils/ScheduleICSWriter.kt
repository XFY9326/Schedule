package tool.xfy9326.schedule.utils

import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.kt.APP_ID
import java.text.SimpleDateFormat
import java.util.*

class ScheduleICSWriter {
    companion object {
        private const val CAL_ID = APP_ID
        private const val PRODID = "-//Produced By: $CAL_ID//App Version: ${BuildConfig.VERSION_CODE}"

        private const val BEGIN_CALENDAR = "BEGIN:VCALENDAR"
        private const val END_CALENDAR = "END:VCALENDAR"
        private const val BEGIN_EVENT = "BEGIN:VEVENT"
        private const val END_EVENT = "END:VEVENT"

        private const val PROPERTY_VERSION = "VERSION:2.0"
        private const val PROPERTY_CALSCALE = "CALSCALE:GREGORIAN"
        private const val PROPERTY_PRODID = "PRODID:$PRODID"

        private const val PROPERTY_UID = "UID:"
        private const val PROPERTY_DTSTAMP = "DTSTAMP:"
        private const val PROPERTY_RRULE = "RRULE:"
        private const val PROPERTY_SUMMARY = "SUMMARY:"
        private const val PROPERTY_LOCATION = "LOCATION:"
        private const val PROPERTY_DESCRIPTION = "DESCRIPTION:"
        private const val PROPERTY_DTSTART = "DTSTART:"
        private const val PROPERTY_DTEND = "DTEND:"

        private val ESCAPE_CHAR_ARR = arrayOf(":", ",", ";")
        private val UTC_TIMEZONE = TimeZone.getTimeZone("UTC")

        private fun createEventUID() = "$CAL_ID-${UUID.randomUUID()}"

        private fun escape(str: String): String {
            var result = str
            for (escapeChar in ESCAPE_CHAR_ARR) {
                result = result.replace(escapeChar, "\\$escapeChar")
            }
            return result
        }
    }

    private val formatter = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.getDefault()).apply {
        timeZone = UTC_TIMEZONE
    }
    private val createTime = Date()
    private val builder = StringBuilder()

    init {
        builder.appendLine(BEGIN_CALENDAR)
        writeHeader()
    }

    private fun writeHeader() {
        builder.apply {
            appendLine(PROPERTY_VERSION)
            appendLine(PROPERTY_PRODID)
            appendLine(PROPERTY_CALSCALE)
        }
    }

    fun addEvent(
        startDate: Date,
        endDate: Date,
        summary: String,
        location: String? = null,
        description: String? = null,
        rrule: RRULE? = null,
    ) {
        builder.apply {
            appendLine(BEGIN_EVENT)
            append(PROPERTY_DTSTAMP).appendLine(formatter.format(createTime))
            append(PROPERTY_UID).appendLine(createEventUID())
            append(PROPERTY_DTSTART).appendLine(formatter.format(startDate))
            append(PROPERTY_DTEND).appendLine(formatter.format(endDate))
            append(PROPERTY_SUMMARY).appendLine(escape(summary))
            location?.let {
                append(PROPERTY_LOCATION).appendLine(escape(it))
            }
            description?.let {
                append(PROPERTY_DESCRIPTION).appendLine(escape(it))
            }
            rrule?.let {
                append(PROPERTY_RRULE).appendLine(rrule.text)
            }
            appendLine(END_EVENT)
        }
    }

    fun build(): String {
        builder.appendLine(END_CALENDAR)
        return builder.toString()
    }

    class RRULE(interval: Int, count: Int, weekDay: WeekDay, firstDayOfWeek: WeekDay) {
        val text = "FREQ=WEEKLY;COUNT=$count;INTERVAL=$interval;BYDAY=${weekDay.shortName};WKST=${firstDayOfWeek.shortName}"
    }
}