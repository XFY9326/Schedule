package tool.xfy9326.schedule.content.adapters.processor

import io.github.xfy9326.atools.base.asArray
import tool.xfy9326.schedule.annotation.ExternalCourseProcessor
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.beans.WebPageContent
import tool.xfy9326.schedule.content.adapters.config.SCUJCCImportConfig
import tool.xfy9326.schedule.content.adapters.parser.SCUJCCCourseParser
import tool.xfy9326.schedule.content.adapters.provider.SCUJCCCourseProvider
import tool.xfy9326.schedule.content.base.AbstractExternalCourseProcessor
import tool.xfy9326.schedule.content.beans.ExternalCourseImportData
import tool.xfy9326.schedule.content.utils.CourseImportHelper

@ExternalCourseProcessor(SCUJCCExternalProcessor.PROCESSOR_NAME)
class SCUJCCExternalProcessor : AbstractExternalCourseProcessor<SCUJCCCourseProvider, SCUJCCCourseParser, SCUJCCImportConfig>(SCUJCCImportConfig::class) {
    companion object {
        const val PROCESSOR_NAME = "SCUJCC"
    }

    override suspend fun onImportCourse(data: ExternalCourseImportData, provider: SCUJCCCourseProvider, parser: SCUJCCCourseParser): ScheduleImportContent? {
        val content = WebPageContent(iframeContent = data.fileContentList.first().asArray())
        val pair = CourseImportHelper.analyseWebPage(content, provider)
        return if (pair == null) {
            null
        } else {
            CourseImportHelper.parseWebCourse(pair.second, pair.first, parser)
        }
    }
}