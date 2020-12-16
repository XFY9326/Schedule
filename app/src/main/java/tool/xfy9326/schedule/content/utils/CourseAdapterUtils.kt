@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*

object CourseAdapterUtils {
    fun getDefaultHttpClient() = HttpClient(Android) {
        install(HttpCookies)
        install(HttpRedirect) {
            // Fix Http 302 Post Error
            checkHttpMethod = false
        }
        BrowserUserAgent()
    }
}