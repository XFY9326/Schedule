@file:Suppress("unused")

package tool.xfy9326.schedule.kt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import androidx.preference.Preference
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import java.util.*
import kotlin.math.min

inline fun buildBundle(crossinline block: Bundle.() -> Unit) = Bundle().apply(block)

fun Uri.deleteFile(context: Context) = DocumentFile.fromSingleUri(context, this)?.delete() == true

inline fun broadcastReceiver(crossinline receiver: (Context?, Intent?) -> Unit) =
    object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            receiver(context, intent)
        }
    }

fun Calendar.getWeekDay() = WeekDay.fromCalWeekDay(get(Calendar.DAY_OF_WEEK))

inline fun Array<Course>.iterateAll(action: (Course, CourseTime) -> Unit) {
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

fun AbstractSettingsFragment.setOnPrefClickListener(key: String, action: (Preference) -> Unit) {
    findPreference<Preference>(key)?.setOnPreferenceClickListener {
        action(it)
        false
    }
}