package tool.xfy9326.schedule.utils.schedule

import tool.xfy9326.schedule.content.base.*

object CourseImportUtils {
    enum class ImportMethod {
        LOGIN_IMPORT,
        NETWORK_IMPORT,
        WEB_IMPORT
    }

    fun getCourseImportMethod(
        config: AbstractCourseImportConfig<*, *, *, *>,
        onInvalidParser: () -> Unit,
        onInterfaceProviderError: () -> Unit,
        onUnknownProviderError: () -> Unit,
    ): ImportMethod? {
        when {
            config.validateProviderType(LoginCourseProvider::class) -> {
                if (config.validateParserType(NetworkCourseParser::class)) {
                    return ImportMethod.LOGIN_IMPORT
                } else {
                    onInvalidParser()
                }
            }
            config.validateProviderType(WebCourseProvider::class) -> {
                if (config.validateParserType(WebCourseParser::class)) {
                    return ImportMethod.WEB_IMPORT
                } else {
                    onInvalidParser()
                }
            }
            config.validateProviderType(NetworkCourseProvider::class) -> {
                if (config.validateParserType(NetworkCourseParser::class)) {
                    return ImportMethod.NETWORK_IMPORT
                } else {
                    onInvalidParser()
                }
            }
            config.validateProviderType(AbstractCourseProvider::class) -> onInterfaceProviderError()
            else -> onUnknownProviderError()
        }
        return null
    }
}