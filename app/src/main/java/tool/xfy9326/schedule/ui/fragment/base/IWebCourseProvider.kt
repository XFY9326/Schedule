package tool.xfy9326.schedule.ui.fragment.base

import android.webkit.WebView

class IWebCourseProvider {
    interface IActivityContact {
        fun onSetupWebView(webView: WebView)

        fun onImportCourseToSchedule(isCurrentSchedule: Boolean)
    }

    interface IFragmentContact {
        fun evaluateJavascript(content: String, callback: ((String?) -> Unit)? = null)

        fun refresh()

        fun setWebViewConnection(enabled: Boolean, autoRefresh: Boolean = true)
    }
}