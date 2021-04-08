@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*

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
        engine {
            config {
                if (hasRedirect) {
                    followRedirects(true)
                    followSslRedirects(true)
                }
                retryOnConnectionFailure(true)
            }
        }
    }
}