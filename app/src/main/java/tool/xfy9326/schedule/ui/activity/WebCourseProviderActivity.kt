package tool.xfy9326.schedule.ui.activity

import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.core.os.bundleOf
import androidx.fragment.app.commitNow
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.WebCourseImportParams
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.databinding.ActivityFragmentContainerBinding
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.tools.livedata.observeEvent
import tool.xfy9326.schedule.ui.activity.base.CourseProviderActivity
import tool.xfy9326.schedule.ui.dialog.FullScreenLoadingDialog
import tool.xfy9326.schedule.ui.fragment.WebCourseProviderFragment
import tool.xfy9326.schedule.ui.fragment.base.IWebCourseProvider
import tool.xfy9326.schedule.ui.vm.WebCourseProviderViewModel
import tool.xfy9326.schedule.ui.vm.base.CourseProviderViewModel
import tool.xfy9326.schedule.utils.JSBridge
import tool.xfy9326.schedule.utils.view.DialogUtils
import tool.xfy9326.schedule.utils.view.ViewUtils

class WebCourseProviderActivity :
    CourseProviderActivity<WebCourseImportParams, WebCourseProvider<*>, WebCourseParser<*>, WebCourseProviderViewModel, ActivityFragmentContainerBinding>(),
    FullScreenLoadingDialog.OnRequestCancelListener, IWebCourseProvider.IActivityContact {

    private val loadingDialogController = FullScreenLoadingDialog.createControllerInstance(this)
    private lateinit var iFragmentContact: IWebCourseProvider.IFragmentContact

    override val exitIfImportSuccess = false
    override val vmClass = WebCourseProviderViewModel::class
    override fun onCreateViewBinding() = ActivityFragmentContainerBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityFragmentContainerBinding, viewModel: WebCourseProviderViewModel) {
        super.onPrepare(viewBinding, viewModel)

        setSupportActionBar(viewBinding.toolBarFragmentContainer.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBindLiveData(viewBinding: ActivityFragmentContainerBinding, viewModel: WebCourseProviderViewModel) {
        super.onBindLiveData(viewBinding, viewModel)

        viewModel.validateHtmlPage.observeEvent(this) {
            if (it == null) {
                viewBinding.layoutFragmentContainer.showShortSnackBar(R.string.invalid_course_import_page)
            } else {
                requestImportCourse(it)
            }
        }
    }

    override fun onInitView(viewBinding: ActivityFragmentContainerBinding, viewModel: WebCourseProviderViewModel) {
        iFragmentContact = WebCourseProviderFragment().apply {
            arguments = bundleOf(
                WebCourseProviderFragment.EXTRA_INIT_PAGE_URL to viewModel.initPageUrl,
                WebCourseProviderFragment.EXTRA_AUTHOR_NAME to viewModel.importConfigInstance.authorName
            )
            supportFragmentManager.commitNow {
                replace(R.id.fragmentContainer, this@apply)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onFullScreenLoadingDialogRequestCancel(): Boolean {
        DialogUtils.showCancelScheduleImportDialog(this) {
            requireViewModel().finishImport()
            loadingDialogController.hide()
        }
        return false
    }

    override fun onBackPressed() {
        if (!iFragmentContact.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onImportCourseToSchedule(isCurrentSchedule: Boolean) {
        iFragmentContact.evaluateJavascript("""
            javascript:
            {
                ${JSBridge.WEB_COURSE_PROVIDER_FUNCTION_SCHEDULE_LOADER}
                ${JSBridge.WEB_COURSE_PROVIDER_FUNCTION_NAME_SCHEDULE_LOADER}($isCurrentSchedule);
            }
        """.trimIndent())
    }

    private fun onGetCurrentHTML(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>, isCurrentSchedule: Boolean) {
        requireViewModel().validateHtmlPage(WebCourseImportParams(htmlContent, iframeContent, frameContent), isCurrentSchedule)
    }

    override fun onReadyImportCourse() {
        loadingDialogController.show()
    }

    override fun onCourseImportFinish(result: CourseProviderViewModel.ImportResult, editScheduleId: Long?) {
        loadingDialogController.hide()
    }

    override fun onShowCourseAdapterError(exception: CourseAdapterException) {
        ViewUtils.showCourseAdapterErrorSnackBar(this, requireViewBinding().layoutFragmentContainer, exception)
    }
}