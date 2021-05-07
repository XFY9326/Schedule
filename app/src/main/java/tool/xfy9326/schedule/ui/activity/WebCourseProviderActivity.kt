package tool.xfy9326.schedule.ui.activity

import android.webkit.JavascriptInterface
import android.webkit.WebView
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.WebCourseImportParams
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.databinding.ActivityFragmentContainerBinding
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.activity.base.AbstractWebCourseProviderActivity
import tool.xfy9326.schedule.ui.dialog.FullScreenLoadingDialog
import tool.xfy9326.schedule.ui.vm.WebCourseProviderViewModel
import tool.xfy9326.schedule.ui.vm.base.CourseProviderViewModel
import tool.xfy9326.schedule.utils.JSBridge
import tool.xfy9326.schedule.utils.view.DialogUtils

class WebCourseProviderActivity :
    AbstractWebCourseProviderActivity<WebCourseImportParams, WebCourseProvider<*>, WebCourseParser<*>, WebCourseProviderViewModel>(),
    FullScreenLoadingDialog.OnRequestCancelListener {
    private val loadingController by lazy { FullScreenLoadingDialog.createControllerInstance(this, supportFragmentManager) }

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

    override fun onSetupWebView(webView: WebView) {
        webView.addJavascriptInterface(object : JSBridge.WebCourseProviderJSInterface {
            @JavascriptInterface
            override fun onReadHtmlContent(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>, isCurrentSchedule: Boolean) {
                onGetCurrentHTML(htmlContent, iframeContent, frameContent, isCurrentSchedule)
            }
        }, JSBridge.WEB_COURSE_PROVIDER_JS_INTERFACE_NAME)
    }

    override fun onFullScreenLoadingDialogRequestCancel(): Boolean {
        DialogUtils.showCancelScheduleImportDialog(this) {
            requireViewModel().finishImport()
            loadingController.hide()
        }
        return false
    }

    override fun onImportCourseToSchedule(isCurrentSchedule: Boolean) {
        fragmentContact.evaluateJavascript(JSBridge.buildWebCourseProviderJS(isCurrentSchedule))
    }

    private fun onGetCurrentHTML(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>, isCurrentSchedule: Boolean) {
        requireViewModel().validateHtmlPage(WebCourseImportParams(htmlContent, iframeContent, frameContent), isCurrentSchedule)
    }

    override fun onReadyImportCourse() {
        loadingController.show()
    }

    override fun onCourseImportFinish(result: CourseProviderViewModel.ImportResult, editScheduleId: Long?) {
        loadingController.hide()
    }
}