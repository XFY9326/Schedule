package tool.xfy9326.schedule

import org.junit.Test
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.utils.CalendarUtils
import tool.xfy9326.schedule.utils.CourseTimeUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun test() {
        val start = CalendarUtils.getCalendar(clearToDate = true).apply {
            set(2020, Calendar.NOVEMBER, 30)
        }.time
        val end = CourseTimeUtils.getTermEndDate(start, WeekDay.MONDAY, 0)
        println(SimpleDateFormat("yyyy-MM-dd").format(end))
    }
}