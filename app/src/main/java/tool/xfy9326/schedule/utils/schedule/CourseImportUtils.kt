package tool.xfy9326.schedule.utils.schedule

import tool.xfy9326.schedule.content.base.AbstractCourseProvider
import tool.xfy9326.schedule.content.base.LoginCourseProvider
import tool.xfy9326.schedule.content.base.NetworkCourseParser
import tool.xfy9326.schedule.content.base.NetworkCourseProvider
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.content.js.JSCourseParser
import tool.xfy9326.schedule.content.js.JSCourseProvider
import tool.xfy9326.schedule.content.utils.BaseCourseImportConfig

object CourseImportUtils {
    enum class ImportMethod {
        LOGIN_IMPORT,
        NETWORK_IMPORT,
        WEB_IMPORT,
        WEB_JS_IMPORT
    }

    fun getCourseImportMethod(
        config: BaseCourseImportConfig,
        onInvalidParser: () -> Unit,
        onInterfaceProviderError: () -> Unit,
        onUnknownProviderError: () -> Unit,
    ): ImportMethod? {
        when {
            config.isProviderType(LoginCourseProvider::class) -> {
                if (config.isParserType(NetworkCourseParser::class)) {
                    return ImportMethod.LOGIN_IMPORT
                } else {
                    onInvalidParser()
                }
            }

            config.isProviderType(WebCourseProvider::class) -> {
                if (config.isParserType(WebCourseParser::class)) {
                    return ImportMethod.WEB_IMPORT
                } else {
                    onInvalidParser()
                }
            }

            config.isProviderType(JSCourseProvider::class) -> {
                if (config.isParserType(JSCourseParser::class)) {
                    return ImportMethod.WEB_JS_IMPORT
                } else {
                    onInvalidParser()
                }
            }

            config.isProviderType(NetworkCourseProvider::class) -> {
                if (config.isParserType(NetworkCourseParser::class)) {
                    return ImportMethod.NETWORK_IMPORT
                } else {
                    onInvalidParser()
                }
            }

            config.isProviderType(AbstractCourseProvider::class) -> onInterfaceProviderError()
            else -> onUnknownProviderError()
        }
        return null
    }
}