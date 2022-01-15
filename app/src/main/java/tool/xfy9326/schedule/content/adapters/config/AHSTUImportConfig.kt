package tool.xfy9326.schedule.content.adapters.config

import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.annotation.CourseImportConfig
import tool.xfy9326.schedule.content.adapters.parser.AHSTUCourseParser
import tool.xfy9326.schedule.content.adapters.provider.AHSTUCourseProvider
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig

@CourseImportConfig
class AHSTUImportConfig : AbstractCourseImportConfig<Nothing, AHSTUCourseProvider, Nothing, AHSTUCourseParser>(
    schoolNameResId = R.string.school_an_hui_science_and_technology_university,
    authorNameResId = R.string.adapter_author_xuke,
    systemNameResId = R.string.system_sso,
    providerClass = AHSTUCourseProvider::class,
    parserClass = AHSTUCourseParser::class,
    sortingBasis = "an_hui_science_and_technology_university"
)