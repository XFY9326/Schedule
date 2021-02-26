package tool.xfy9326.schedule.content.adapters

import tool.xfy9326.schedule.content.adapters.config.NAUSSOImportConfig
import tool.xfy9326.schedule.content.adapters.config.NAUWebImportConfig
import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.base.AbstractCourseProvider
import tool.xfy9326.schedule.content.base.CourseImportConfig
import kotlin.reflect.KClass

object CourseImportConfigRegistry {
    val configs: List<KClass<out CourseImportConfig<*, out AbstractCourseProvider<*>, *, out AbstractCourseParser<*>>>> =
        listOf(
            NAUSSOImportConfig::class,
            NAUWebImportConfig::class
        )
}