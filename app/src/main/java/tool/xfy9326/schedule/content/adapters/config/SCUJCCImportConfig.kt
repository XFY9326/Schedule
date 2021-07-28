package tool.xfy9326.schedule.content.adapters.config

import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.annotation.CourseImportConfig
import tool.xfy9326.schedule.content.adapters.parser.SCUJCCCourseParser
import tool.xfy9326.schedule.content.adapters.provider.SCUJCCCourseProvider
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig

@CourseImportConfig
class SCUJCCImportConfig : AbstractCourseImportConfig<Nothing, SCUJCCCourseProvider, Nothing, SCUJCCCourseParser>(
    schoolNameResId = R.string.school_cheng_du_jin_cheng_college,
    authorNameResId = R.string.adapter_author_xfy9326,
    systemNameResId = R.string.system_zhen_fang,
    providerClass = SCUJCCCourseProvider::class,
    parserClass = SCUJCCCourseParser::class,
    sortingBasis = "ChengDuJinChengXueYuan"
)