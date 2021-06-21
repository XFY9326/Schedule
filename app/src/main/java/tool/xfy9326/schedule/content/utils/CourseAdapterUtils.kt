@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import java.text.SimpleDateFormat
import java.util.*

object CourseAdapterUtils {

    fun buildSimpleHttpClient(supportJson: Boolean = false, hasRedirect: Boolean = true) = HttpClient(OkHttp) {
        install(HttpCookies)
        if (hasRedirect) {
            install(HttpRedirect) {
                // 修复 Http 302 Post 错误
                checkHttpMethod = false
            }
        }
        if (supportJson) {
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }
        BrowserUserAgent()
    }

    fun newDateFormat(format: String = "yyyy-MM-dd"): SimpleDateFormat {
        return SimpleDateFormat(format, Locale.getDefault())
    }

    fun simpleTermFix(termStart: Date?, termEnd: Date?) =
        if (termStart != null && termEnd == null) {
            termStart to termStart
        } else if (termStart != null && termEnd != null) {
            termStart to termEnd
        } else {
            null
        }
}