package tool.xfy9326.schedule.content

import tool.xfy9326.schedule.content.adapters.config.NAUSSOImportConfig
import tool.xfy9326.schedule.content.adapters.config.NAUWebImportConfig

object CourseAdapterConfig {
    private val registerConfigs = arrayOf(
        NAUWebImportConfig(),
        NAUSSOImportConfig()
    ).sortedBy {
        it.lowerCaseSortingBasis
    }

    val size = registerConfigs.size

    operator fun get(index: Int) = registerConfigs[index]
}