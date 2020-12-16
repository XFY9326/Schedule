package tool.xfy9326.schedule.content

import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.content.adapters.config.NAUSSOImportConfig
import tool.xfy9326.schedule.content.adapters.config.NAUWebImportConfig

object CourseAdapterConfig {
    val metas = arrayOf(
        NAUWebImportConfig(),
        NAUSSOImportConfig()
    ).sortedBy {
        it.getSchoolNameWords(App.instance) + it.getSystemNameWords(App.instance)
    }
}