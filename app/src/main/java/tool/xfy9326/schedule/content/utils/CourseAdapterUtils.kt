@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*

object CourseAdapterUtils {

    /**
     * For HttpRedirect, fix Http 302 Post Error
     */
    fun getDefaultHttpClient() = HttpClient(OkHttp) {
        install(HttpCookies)
        install(HttpRedirect) {
            // Fix Http 302 Post Error
            checkHttpMethod = false
        }
        BrowserUserAgent()
    }
}