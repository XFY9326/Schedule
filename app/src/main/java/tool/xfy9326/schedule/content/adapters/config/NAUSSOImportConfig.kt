package tool.xfy9326.schedule.content.adapters.config

import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.adapters.parser.NAUCourseLoginParser
import tool.xfy9326.schedule.content.adapters.provider.NAUSSOCourseProvider
import tool.xfy9326.schedule.content.base.CourseImportConfig

class NAUSSOImportConfig : CourseImportConfig<NAUSSOCourseProvider, NAUCourseLoginParser>(
    schoolName = R.string.school_nanjing_audit_university,
    authorName = R.string.adapter_author_xfy9326,
    systemName = R.string.system_nau_sso,
    staticImportOptions = R.array.adapter_options_term,
    providerClass = NAUSSOCourseProvider::class.java,
    parserClass = NAUCourseLoginParser::class.java
)