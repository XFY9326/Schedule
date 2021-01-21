package tool.xfy9326.schedule.content

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.content.adapters.config.NAUSSOImportConfig
import tool.xfy9326.schedule.content.adapters.config.NAUWebImportConfig

object CourseAdapterConfig {
    private val registerConfigs = arrayOf(
        NAUWebImportConfig(),
        NAUSSOImportConfig()
    )

    private val sortedConfigs by lazy {
        registerConfigs.sortedBy {
            it.schoolNamePinyin + it.systemNamePinyin
        }
    }

    suspend fun getConfigs() = withContext(Dispatchers.Default) {
        return@withContext sortedConfigs
    }
}