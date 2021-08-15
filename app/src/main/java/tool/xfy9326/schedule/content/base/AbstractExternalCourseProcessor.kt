package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.content.beans.ExternalCourseImportData
import kotlin.reflect.KClass

abstract class AbstractExternalCourseProcessor<T1 : AbstractCourseProvider<*>, T2 : AbstractCourseParser<*>, C : AbstractCourseImportConfig<*, T1, *, T2>>(val configClass: KClass<C>) {
    /**
     * On import course
     *
     * @param data 外部传入数据
     * @param provider 课程提供器
     * @param parser 课程解析器
     * @return 若传入的数据导致无法解析出结果，返回null
     */
    abstract fun onImportCourse(data: ExternalCourseImportData, provider: T1, parser: T2): ScheduleImportContent?
}