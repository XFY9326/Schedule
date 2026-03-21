package tool.xfy9326.schedule.utils

import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.CalendarEventDescription
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.SectionTime.Companion.description
import tool.xfy9326.schedule.tools.io.IOManager

object CalendarEventUtils {
    fun getEventDescription(course: Course, courseTime: CourseTime, eventDescriptions: Set<CalendarEventDescription>): String? {
        if (eventDescriptions.isEmpty()) return null
        return buildList {
            if (CalendarEventDescription.SECTIONS in eventDescriptions) {
                add(IOManager.resources.getString(R.string.ics_description_sections, courseTime.sectionTime.description))
            }
            course.teacher?.takeIf { CalendarEventDescription.TEACHER in eventDescriptions }?.let {
                add(IOManager.resources.getString(R.string.ics_description_teacher, it))
            }
            courseTime.location?.takeIf { CalendarEventDescription.LOCATION in eventDescriptions }?.let {
                add(IOManager.resources.getString(R.string.ics_description_location, it))
            }
        }.joinToString(NEW_LINE)
    }
}