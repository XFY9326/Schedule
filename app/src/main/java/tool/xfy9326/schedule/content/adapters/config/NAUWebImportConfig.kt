package tool.xfy9326.schedule.content.adapters.config

import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.adapters.parser.NAUCourseWebParser
import tool.xfy9326.schedule.content.adapters.provider.NAUWebCourseProvider
import tool.xfy9326.schedule.content.base.CourseImportConfig

class NAUWebImportConfig : CourseImportConfig<NAUWebCourseProvider, NAUCourseWebParser>(
    schoolName = R.string.school_nanjing_audit_university,
    authorName = R.string.adapter_author_xfy9326,
    systemName = R.string.system_nau_jwc_web,
    providerClass = NAUWebCourseProvider::class.java,
    parserClass = NAUCourseWebParser::class.java
)