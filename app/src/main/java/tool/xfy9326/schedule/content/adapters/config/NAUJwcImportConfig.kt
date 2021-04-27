@file:Suppress("SpellCheckingInspection")

package tool.xfy9326.schedule.content.adapters.config

import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.annotation.CourseImportConfig
import tool.xfy9326.schedule.content.adapters.parser.NAUCourseParser
import tool.xfy9326.schedule.content.adapters.provider.NAUJwcCourseProvider
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig

@CourseImportConfig
class NAUJwcImportConfig : AbstractCourseImportConfig<Nothing, NAUJwcCourseProvider, Nothing, NAUCourseParser>(
    schoolNameResId = R.string.school_nanjing_audit_university,
    authorNameResId = R.string.adapter_author_xfy9326,
    systemNameResId = R.string.system_nau_jwc,
    staticImportOptionsResId = R.array.adapter_options_term,
    providerClass = NAUJwcCourseProvider::class,
    parserClass = NAUCourseParser::class,
    sortingBasis = "NanJingShenJiDaXueJwc"
)