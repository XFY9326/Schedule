package tool.xfy9326.schedule.content.beans

import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.base.AbstractCourseProvider

class CourseImportInstance<P1 : AbstractCourseProvider<*>, P2 : AbstractCourseParser<*>> constructor(
    val schoolName: String,
    val authorName: String,
    val systemName: String,
    val staticImportOptions: Array<String>? = null,
    val provider: P1,
    val parser: P2,
)