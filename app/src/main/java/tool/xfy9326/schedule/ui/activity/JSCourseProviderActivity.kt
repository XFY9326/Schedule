package tool.xfy9326.schedule.ui.activity

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.lifecycle.lifecycleScope
import io.github.xfy9326.atools.livedata.observeEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.ScheduleImportRequestParams
import tool.xfy9326.schedule.content.js.JSCourseParser
import tool.xfy9326.schedule.content.js.JSCourseProvider
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.databinding.ActivityFragmentContainerBinding
import tool.xfy9326.schedule.ui.activity.base.AbstractWebCourseProviderActivity
import tool.xfy9326.schedule.ui.dialog.FullScreenLoadingDialog
import tool.xfy9326.schedule.ui.vm.JSCourseProviderViewModel
import tool.xfy9326.schedule.utils.JSBridge
import tool.xfy9326.schedule.utils.schedule.ScheduleImportManager
import tool.xfy9326.schedule.utils.showSnackBar
import tool.xfy9326.schedule.utils.view.DialogUtils

class JSCourseProviderActivity : AbstractWebCourseProviderActivity<String, JSCourseProvider, JSCourseParser, JSCourseProviderViewModel>() {
    private val loadingController by lazy { FullScreenLoadingDialog.Controller.newInstance(this, supportFragmentManager) }
    private val enableJSNetwork = runBlocking { AppSettingsDataStore.jsCourseImportEnableNetworkFlow.first() }

    override val vmClass = JSCourseProviderViewModel::class

    override fun onBindLiveData(viewBinding: ActivityFragmentContainerBinding, viewModel: JSCourseProviderViewModel) {
        super.onBindLiveData(viewBinding, viewModel)
        viewModel.jsContent.observeEvent(this, observer = ::onJSLoaded)
        viewModel.providerError.observeEvent(this, javaClass.simpleName) {
            loadingController.hide()
        }
    }

    override fun onInitView(viewBinding: ActivityFragmentContainerBinding, viewModel: JSCourseProviderViewModel) {
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
        webView.addJavascriptInterface(object : JSBridge.JSCourseProviderJSInterface {
            @JavascriptInterface
            override fun onJSProviderResponse(resultJSON: String, isCurrentSchedule: Boolean) {
                lifecycleScope.launch {
                    requestImportCourse(ScheduleImportRequestParams(isCurrentSchedule, resultJSON))
                }
            }
        }, JSBridge.JS_COURSE_PROVIDER_JS_INTERFACE_NAME)
    }

    override fun onImportCourseToSchedule(isCurrentSchedule: Boolean) {
        requireViewModel().requestJSContent(isCurrentSchedule)
    }

    private fun onJSLoaded(jsContent: String) {
        fragmentContact.evaluateJavascript(jsContent) {
            if (it != JSBridge.SCRIPT_EXECUTE_SUCCESS_RESULT) {
                requireViewBinding().layoutFragmentContainer.showSnackBar(R.string.js_syntax_error)
            }
        }
    }

    override fun onReadyImportCourse() {
        loadingController.show()
        if (!(requireViewModel().isRequireNetwork && enableJSNetwork)) {
            fragmentContact.setWebViewConnection(false)
        }
    }

    override fun onCourseImportFinish(result: ScheduleImportManager.ImportResult, editScheduleId: Long?) {
        if (!(requireViewModel().isRequireNetwork && enableJSNetwork)) {
            fragmentContact.setWebViewConnection(true)
        } else {
            fragmentContact.refresh()
        }
        loadingController.hide()
    }
}