package tool.xfy9326.schedule.utils

import android.net.Uri

object AppUriUtils {
    private const val SCHEME_PURE_SCHEDULE = "pusc"
    private const val HOST_COURSE_IMPORT = "course_import"
    private const val PATH_JS_CONFIG = "js_config"
    private const val ARG_SRC = "src"

    private fun Uri.parseScheme(): Uri? = takeIf {
        scheme.equals(SCHEME_PURE_SCHEDULE, true)
    }

    private fun Uri.parseCourseImport(): Uri? = takeIf {
        host.equals(HOST_COURSE_IMPORT, true)
    }

    fun isJSCourseImportUri(data: Uri): Boolean =
        data.parseScheme()?.parseCourseImport() != null

    fun tryParseJSCourseImport(data: Uri?): String? =
        data?.parseScheme()?.parseCourseImport()?.takeIf {
            it.pathSegments.let { p -> p.size == 1 && p.first().equals(PATH_JS_CONFIG, true) }
        }?.getQueryParameter(ARG_SRC)
}