package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.content.beans.CourseImportInstance
import tool.xfy9326.schedule.content.beans.ExternalCourseImportData
import kotlin.reflect.KClass

abstract class AbstractExternalCourseProcessor<T1 : AbstractCourseProvider<*>, T2 : AbstractCourseParser<*>, C : AbstractCourseImportConfig<*, T1, *, T2>>(private val configClass: KClass<C>) {
    private val configInstance: CourseImportInstance<T1, T2> by lazy {
        configClass.java.newInstance().getInstance()
    }
    val adapterInfo: IAdapterInfo
        get() = object : IAdapterInfo {
            override val schoolName = configInstance.schoolName
            override val systemName = configInstance.systemName
            override val authorName = configInstance.authorName
        }

    suspend fun importCourse(data: ExternalCourseImportData) =
        onImportCourse(data, configInstance.provider, configInstance.parser)

    /**
     * On import course
     *
     * @param data 外部传入数据
     * @param provider 课程提供器
     * @param parser 课程解析器
     * @return 若传入的数据导致无法解析出结果，返回null
     */
    protected abstract suspend fun onImportCourse(data: ExternalCourseImportData, provider: T1, parser: T2): ScheduleImportContent?
}