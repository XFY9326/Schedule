package tool.xfy9326.schedule.beans

import tool.xfy9326.schedule.content.utils.CourseParseResult
import java.util.*

class ScheduleImportContent(val scheduleTimes: List<ScheduleTime>, val coursesParserResult: CourseParseResult, val term: Pair<Date, Date>? = null)