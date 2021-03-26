package tool.xfy9326.schedule.content

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig
import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.base.AbstractCourseProvider

object CourseAdapterManager {
    suspend fun getSortedConfigs() = withContext(Dispatchers.Default) {
        CourseImportConfigRegistry.configs.map {
            it.java.newConfigInstance()
        }.sortedBy {
            it.lowerCaseSortingBasis
        }
    }

    fun <T : AbstractCourseImportConfig<*, out AbstractCourseProvider<*>, *, out AbstractCourseParser<*>>> Class<T>.newConfigInstance(): T =
        newInstance()
}