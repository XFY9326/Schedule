package tool.xfy9326.schedule.content

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.content.base.ICourseImportConfig
import tool.xfy9326.schedule.io.JSFileManager

object CourseAdapterManager {
    suspend fun getAllConfigs(): List<ICourseImportConfig> = withContext(Dispatchers.IO) {
        val localConfigs = CourseImportConfigRegistry.getConfigs()
        val jsConfigs = JSFileManager.loadJSConfigs()
        return@withContext if (jsConfigs != null) {
            localConfigs + jsConfigs
        } else {
            localConfigs
        }.sortedBy { it.lowerCaseSortingBasis }
    }
}