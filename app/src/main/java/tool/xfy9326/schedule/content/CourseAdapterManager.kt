package tool.xfy9326.schedule.content

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.content.adapters.CourseImportConfigRegistry
import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.base.AbstractCourseProvider
import tool.xfy9326.schedule.content.base.CourseImportConfig

object CourseAdapterManager {
    suspend fun getSortedConfigs() = withContext(Dispatchers.Default) {
        CourseImportConfigRegistry.configs.map {
            it.java.newConfigInstance()
        }.sortedBy {
            it.lowerCaseSortingBasis
        }
    }

    fun <T : CourseImportConfig<*, out AbstractCourseProvider<*>, *, out AbstractCourseParser<*>>> Class<T>.newConfigInstance(): T =
        newInstance()
}