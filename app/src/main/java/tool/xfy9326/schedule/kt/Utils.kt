package tool.xfy9326.schedule.kt

import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import tool.xfy9326.schedule.beans.*
import java.util.*
import kotlin.math.min

fun Calendar.getWeekDay() = WeekDay.valueOfCalWeekDay(get(Calendar.DAY_OF_WEEK))

inline fun List<Course>.iterateAll(action: (Course, CourseTime) -> Unit) {
    for (course in this) for (time in course.times) action(course, time)
}

infix fun ClassTime.intersect(classTime: ClassTime): Boolean =
    weekDay == classTime.weekDay && classStartTime <= classTime.classEndTime && classEndTime >= classTime.classStartTime

infix fun CourseTime.intersect(courseTime: CourseTime): Boolean {
    for (i in 0 until min(weekNum.size, courseTime.weekNum.size)) {
        if (weekNum[i] && courseTime.weekNum[i] && classTime intersect courseTime.classTime) return true
    }
    return false
}

infix fun ScheduleTime.intersect(scheduleTime: ScheduleTime): Boolean {
    var start1 = this.startHour * 60 + this.startMinute
    val end1 = this.endHour * 60 + this.endMinute
    if (start1 > end1) {
        start1 -= 24 * 60
    }

    var start2 = scheduleTime.startHour * 60 + scheduleTime.startMinute
    val end2 = scheduleTime.endHour * 60 + scheduleTime.endMinute
    if (start2 > end2) {
        start2 -= 24 * 60
    }

    return start1 <= end2 && end1 >= start2
}

fun <T : Preference> PreferenceFragmentCompat.findPreference(@StringRes keyId: Int): T? = findPreference(getString(keyId))

fun PreferenceFragmentCompat.setOnPrefClickListener(@StringRes keyId: Int, action: (Preference) -> Unit) {
    findPreference<Preference>(keyId)?.setOnPreferenceClickListener {
        action(it)
        false
    }
}

inline fun <reified T : PreferenceFragmentCompat> PreferenceFragmentCompat.bindPrefFragment(@StringRes keyId: Int) {
    findPreference<Preference>(keyId)?.fragment = T::class.qualifiedName
}