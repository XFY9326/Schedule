package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.beans.JSParams

class JSCourseImportConfig(jsConfig: JSConfig) : AbstractCourseImportConfig<JSParams, JSCourseProvider, JSParams, JSCourseParser>(
    schoolName = jsConfig.schoolName,
    authorName = jsConfig.authorName,
    systemName = jsConfig.systemName,
    providerClass = JSCourseProvider::class.java,
    parserClass = JSCourseParser::class.java,
    providerParams = jsConfig.getJSParams(),
    parserParams = jsConfig.getJSParams(),
    sortingBasis = jsConfig.sortingBasis
) {
    companion object {
        fun JSConfig.toCourseImportConfig() = JSCourseImportConfig(this)
    }
}