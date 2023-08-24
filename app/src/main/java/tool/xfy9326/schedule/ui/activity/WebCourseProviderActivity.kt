package tool.xfy9326.schedule.ui.activity

import android.webkit.JavascriptInterface
import android.webkit.WebView
import io.github.xfy9326.atools.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.WebPageContent
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.databinding.ActivityFragmentContainerBinding
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.activity.base.AbstractWebCourseProviderActivity
import tool.xfy9326.schedule.ui.dialog.FullScreenLoadingDialog
import tool.xfy9326.schedule.ui.vm.WebCourseProviderViewModel
import tool.xfy9326.schedule.utils.JSBridge
import tool.xfy9326.schedule.utils.schedule.ScheduleImportManager
import tool.xfy9326.schedule.utils.view.DialogUtils

class WebCourseProviderActivity :
    AbstractWebCourseProviderActivity<WebPageContent, WebCourseProvider<*>, WebCourseParser<*>, WebCourseProviderViewModel>() {
    private val loadingController by lazy { FullScreenLoadingDialog.Controller.newInstance(this, supportFragmentManager) }

    override val vmClass = WebCourseProviderViewModel::class

    override fun onBindLiveData(viewBinding: ActivityFragmentContainerBinding, viewModel: WebCourseProviderViewModel) {
        super.onBindLiveData(viewBinding, viewModel)

        viewModel.validateHtmlPage.observeEvent(this) {
            if (it == null) {
                viewBinding.layoutFragmentContainer.showSnackBar(R.string.invalid_course_import_page)
            } else {
                requestImportCourse(it)
            }
        }
    }

    override fun onInitView(viewBinding: ActivityFragmentContainerBinding, viewModel: WebCourseProviderViewModel) {
        super.onInitView(viewBinding, viewModel)
        loadingController.setOnRequestCancelListener {
            DialogUtils.showCancelScheduleImportDialog(this) {
                viewModel.finishImport()
                loadingController.hide()
            }
            false
        }
    }

    override fun onSetupWebView(webView: WebView) {
        webView.addJavascriptInterface(object : JSBridge.WebCourseProviderJSInterface {
            @JavascriptInterface
            override fun onReadHtmlContent(
                htmlContent: String,
                iframeContent: Array<String>,
                frameContent: Array<String>,
                isCurrentSchedule: Boolean
            ) {
                onGetCurrentHTML(htmlContent, iframeContent, frameContent, isCurrentSchedule)
            }
        }, JSBridge.WEB_COURSE_PROVIDER_JS_INTERFACE_NAME)
    }

    override fun onImportCourseToSchedule(isCurrentSchedule: Boolean) {
        fragmentContact.evaluateJavascript(JSBridge.buildWebCourseProviderJS(isCurrentSchedule)) {
            if (it != JSBridge.SCRIPT_EXECUTE_SUCCESS_RESULT) {
                requireViewBinding().layoutFragmentContainer.showSnackBar(R.string.js_syntax_error)
            }
        }
    }

    private fun onGetCurrentHTML(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>, isCurrentSchedule: Boolean) {
        requireViewModel().validateHtmlPage(WebPageContent(htmlContent, iframeContent, frameContent), isCurrentSchedule)
    }

    override fun onReadyImportCourse() {
        loadingController.show()
    }

    override fun onCourseImportFinish(result: ScheduleImportManager.ImportResult, editScheduleId: Long?) {
        loadingController.hide()
    }
}