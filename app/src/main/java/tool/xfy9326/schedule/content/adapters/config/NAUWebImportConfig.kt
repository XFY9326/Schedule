@file:Suppress("SpellCheckingInspection")

package tool.xfy9326.schedule.content.adapters.config

import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.annotation.CourseImportConfig
import tool.xfy9326.schedule.content.adapters.parser.NAUCourseWebParser
import tool.xfy9326.schedule.content.adapters.provider.NAUWebCourseProvider
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig

@CourseImportConfig
class NAUWebImportConfig : AbstractCourseImportConfig<Nothing, NAUWebCourseProvider, Nothing, NAUCourseWebParser>(
    schoolNameResId = R.string.school_nanjing_audit_university,
    authorNameResId = R.string.adapter_author_xfy9326,
    systemNameResId = R.string.system_sso_web,
    providerClass = NAUWebCourseProvider::class,
    parserClass = NAUCourseWebParser::class,
    sortingBasis = "NanJingShenJiDaXueWeb"
)