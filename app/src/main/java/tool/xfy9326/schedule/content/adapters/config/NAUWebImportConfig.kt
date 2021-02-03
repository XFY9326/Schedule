@file:Suppress("SpellCheckingInspection")

package tool.xfy9326.schedule.content.adapters.config

import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.adapters.parser.NAUCourseWebParser
import tool.xfy9326.schedule.content.adapters.provider.NAUWebCourseProvider
import tool.xfy9326.schedule.content.base.CourseImportConfig

class NAUWebImportConfig : CourseImportConfig<Nothing, NAUWebCourseProvider, NAUCourseWebParser>(
    schoolNameResId = R.string.school_nanjing_audit_university,
    authorNameResId = R.string.adapter_author_xfy9326,
    systemNameResId = R.string.system_nau_jwc_web,
    providerClass = NAUWebCourseProvider::class.java,
    parserClass = NAUCourseWebParser::class.java,
    sortingBasis = "NanJingShenJiDaXueWeb"
)