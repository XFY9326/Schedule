package tool.xfy9326.schedule.ui.activity

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.content.base.JSCourseParser
import tool.xfy9326.schedule.content.base.JSCourseProvider
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.databinding.ActivityFragmentContainerBinding
import tool.xfy9326.schedule.ui.activity.base.AbstractWebCourseProviderActivity
import tool.xfy9326.schedule.ui.dialog.FullScreenLoadingDialog
import tool.xfy9326.schedule.ui.vm.JSCourseProviderViewModel
import tool.xfy9326.schedule.ui.vm.base.CourseProviderViewModel
import tool.xfy9326.schedule.utils.JSBridge
import tool.xfy9326.schedule.utils.view.DialogUtils

class JSCourseProviderActivity : AbstractWebCourseProviderActivity<String, JSCourseProvider, JSCourseParser, JSCourseProviderViewModel>(),
    FullScreenLoadingDialog.OnRequestCancelListener {
    private val loadingController by lazy { FullScreenLoadingDialog.createControllerInstance(this, supportFragmentManager) }
    private val enableJSNetwork = runBlocking { AppSettingsDataStore.jsCourseImportEnableNetworkFlow.first() }

    override val vmClass = JSCourseProviderViewModel::class

    override fun onBindLiveData(viewBinding: ActivityFragmentContainerBinding, viewModel: JSCourseProviderViewModel) {
        super.onBindLiveData(viewBinding, viewModel)
        viewModel.jsContent.observeEvent(this, observer = ::onJSLoaded)
        viewModel.providerError.observeEvent(this, javaClass.simpleName) {
            loadingController.hide()
        }
    }

    override fun onSetupWebView(webView: WebView) {
        webView.addJavascriptInterface(object : JSBridge.JSCourseProviderJSInterface {
            @JavascriptInterface
            override fun onJSProviderResponse(resultJSON: String, isCurrentSchedule: Boolean) {
                lifecycleScope.launch {
                    requestImportCourse(ImportRequestParams(isCurrentSchedule, resultJSON))
                }
            }
        }, JSBridge.JS_COURSE_PROVIDER_JS_INTERFACE_NAME)
    }

    override fun onImportCourseToSchedule(isCurrentSchedule: Boolean) {
        requireViewModel().requestJSContent(isCurrentSchedule)
    }

    private fun onJSLoaded(jsContent: String) {
        fragmentContact.evaluateJavascript(jsContent)
    }

    override fun onReadyImportCourse() {
        loadingController.show()
        if (!(requireViewModel().isRequireNetwork && enableJSNetwork)) {
            fragmentContact.setWebViewConnection(false)
        }
    }

    override fun onCourseImportFinish(result: CourseProviderViewModel.ImportResult, editScheduleId: Long?) {
        if (!(requireViewModel().isRequireNetwork && enableJSNetwork)) {
            fragmentContact.setWebViewConnection(true)
        } else {
            fragmentContact.refresh()
        }
        loadingController.hide()
    }

    override fun onFullScreenLoadingDialogRequestCancel(): Boolean {
        DialogUtils.showCancelScheduleImportDialog(this) {
            requireViewModel().finishImport()
            loadingController.hide()
        }
        return false
    }
}