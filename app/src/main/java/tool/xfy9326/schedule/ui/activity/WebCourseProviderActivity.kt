package tool.xfy9326.schedule.ui.activity

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.*
import androidx.annotation.Keep
import androidx.core.view.isVisible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.base.CourseImportConfig
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.databinding.ActivityWebCourseProviderBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.dialog.FullScreenLoadingDialog
import tool.xfy9326.schedule.ui.dialog.ImportCourseConflictDialog
import tool.xfy9326.schedule.ui.dialog.WebCourseProviderBottomPanel
import tool.xfy9326.schedule.ui.vm.WebCourseProviderViewModel
import tool.xfy9326.schedule.utils.DialogUtils
import tool.xfy9326.schedule.utils.ViewUtils

@SuppressLint("SetJavaScriptEnabled")
class WebCourseProviderActivity : ViewModelActivity<WebCourseProviderViewModel, ActivityWebCourseProviderBinding>(),
    WebCourseProviderBottomPanel.OnWebCourseProviderBottomPanelOperateListener, ImportCourseConflictDialog.OnConfirmImportCourseConflictListener,
    FullScreenLoadingDialog.OnRequestCancelListener {
    companion object {
        const val EXTRA_COURSE_IMPORT_CONFIG = "EXTRA_COURSE_IMPORT_CONFIG"
        private const val EXTRA_WEB_VIEW = "EXTRA_WEB_VIEW"

        private const val HTML_PRINT_JAVASCRIPT_INTERFACE_NAME = "HtmlPrint"
        private const val HTML_PRINT_JAVASCRIPT_METHOD_NAME = "printHTML"
        private const val HTML_PRINT_JAVASCRIPT =
            """
                javascript:
                var currentSchedule = %s;
                
                var htmlContent = document.getElementsByTagName("html")[0].outerHTML;
                
                var iframeTags = document.getElementsByTagName("iframe");
                var iframeList = [];
                for (var i = 0; i < iframeTags.length; i++) {
                    iframeList.push(iframeTags[i].contentDocument.body.parentElement.outerHTML);
                }
                
                var frameTags = document.getElementsByTagName("frame");
                var frameList = [];
                for (var i = 0; i < frameTags.length; i++) {
                    frameList.push(frameTags[i].contentDocument.body.parentElement.outerHTML);
                }
                
                window.$HTML_PRINT_JAVASCRIPT_INTERFACE_NAME.$HTML_PRINT_JAVASCRIPT_METHOD_NAME(htmlContent, iframeList, frameList, currentSchedule);
            """
    }

    override fun onPrepare(viewBinding: ActivityWebCourseProviderBinding, viewModel: WebCourseProviderViewModel) {
        setSupportActionBar(viewBinding.toolBarWebCourseProvider.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.registerConfig(intent.getSerializableExtra(EXTRA_COURSE_IMPORT_CONFIG)
            ?.tryCast<CourseImportConfig<WebCourseProvider, WebCourseParser>>()!!)

        runBlocking {
            if (AppSettingsDataStore.keepWebProviderCacheFlow.first()) clearAll()
        }
    }

    override fun onBindLiveData(viewBinding: ActivityWebCourseProviderBinding, viewModel: WebCourseProviderViewModel) {
        viewModel.providerError.observeEvent(this, observer = ::showCourseAdapterError)
        viewModel.validateHtmlPage.observeEvent(this) {
            if (it == null) {
                viewBinding.layoutWebCourseProvider.showShortSnackBar(R.string.invalid_course_import_page)
            } else {
                onValidatePageSuccess(it.first, it.third, it.second)
            }
        }
        viewModel.courseImportFinish.observeEvent(this) {
            FullScreenLoadingDialog.closeDialog(supportFragmentManager)
            if (it.first) {
                afterSaveCourse(it.second)
            }
        }
    }

    override fun onInitView(viewBinding: ActivityWebCourseProviderBinding, viewModel: WebCourseProviderViewModel) {
        viewBinding.webViewWebCourseProvider.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    changeProgressBar(newProgress)
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    handler?.proceed()
                }
            }
            addJavascriptInterface(object : HTMLPrinterJavaScriptInterface {
                @JavascriptInterface
                override fun printHTML(
                    htmlContent: String,
                    iframeContent: Array<String>,
                    frameContent: Array<String>,
                    currentSchedule: Boolean,
                ) {
                    onGetCurrentHTML(htmlContent, iframeContent, frameContent, currentSchedule)
                }
            }, HTML_PRINT_JAVASCRIPT_INTERFACE_NAME)
        }

        viewBinding.buttonWebCourseProviderPanel.setOnClickListener {
            showBottomPanel()
        }

        if (!viewModel.isBottomPanelInitShowed) {
            showBottomPanel()
            viewModel.isBottomPanelInitShowed = true
        }
    }

    override fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: ActivityWebCourseProviderBinding, viewModel: WebCourseProviderViewModel) {
        if (bundle == null) {
            clearAll()
            viewBinding.webViewWebCourseProvider.loadUrl(viewModel.courseProvider.initPageUrl)
        } else {
            if (WebCourseProviderBottomPanel.isShowing(supportFragmentManager)) {
                viewBinding.buttonWebCourseProviderPanel.isVisible = false
            }
            bundle.getBundle(EXTRA_WEB_VIEW)?.let {
                viewBinding.webViewWebCourseProvider.restoreState(it)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(EXTRA_WEB_VIEW, buildBundle {
            requireViewBinding().webViewWebCourseProvider.saveState(this)
        })
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_web_course_provider, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_webCourseProviderRefresh -> requireViewBinding().webViewWebCourseProvider.reload()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onFullScreenLoadingDialogRequestCancel(): Boolean {
        DialogUtils.showCancelScheduleImportDialog(this) {
            requireViewModel().finishImport()
            FullScreenLoadingDialog.closeDialog(supportFragmentManager)
        }
        return false
    }

    override fun onBackPressed() {
        requireViewBinding().webViewWebCourseProvider.apply {
            if (canGoBack()) {
                goBack()
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun changeProgressBar(newProgress: Int) {
        requireViewBinding().progressBarWebCourseProvider.apply {
            visibility = if (newProgress != 100) View.VISIBLE else View.INVISIBLE
            progress = newProgress
        }
    }

    private fun showBottomPanel() {
        requireViewBinding().buttonWebCourseProviderPanel.apply {
            isVisible = false
            startAnimation(AnimationUtils.loadAnimation(this@WebCourseProviderActivity, R.anim.anim_bottom_button_out))
        }
        WebCourseProviderBottomPanel.showDialog(supportFragmentManager, requireViewModel().importConfig.getAuthorNameText(this))
    }

    private fun getCurrentHTML(currentSchedule: Boolean) {
        requireViewBinding().webViewWebCourseProvider.loadUrl(HTML_PRINT_JAVASCRIPT.format(currentSchedule.toString()))
    }

    override fun onWebCourseProviderBottomPanelDismiss() {
        requireViewBinding().buttonWebCourseProviderPanel.apply {
            isVisible = true
            startAnimation(AnimationUtils.loadAnimation(this@WebCourseProviderActivity, R.anim.anim_bottom_button_in))
        }
    }

    override fun onImportCourseToCurrentSchedule() {
        getCurrentHTML(true)
    }

    override fun onImportCourseToNewSchedule() {
        getCurrentHTML(false)
    }

    override fun onConfirmImportCourseConflict() {
        finish()
    }

    private fun onGetCurrentHTML(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>, currentSchedule: Boolean) {
        requireViewModel().validateHtmlPage(WebCourseProviderViewModel.ImportParams(htmlContent, iframeContent, frameContent), currentSchedule)
    }

    private fun onValidatePageSuccess(importParams: WebCourseProviderViewModel.ImportParams, importOption: Int, currentSchedule: Boolean) {
        if (currentSchedule) {
            DialogUtils.showOverwriteScheduleAttentionDialog(this@WebCourseProviderActivity) {
                FullScreenLoadingDialog.showDialog(supportFragmentManager)
                requireViewModel().importCourse(importParams, importOption, currentSchedule, null)
            }
        } else {
            DialogUtils.showNewScheduleNameDialog(this@WebCourseProviderActivity) {
                FullScreenLoadingDialog.showDialog(supportFragmentManager)
                requireViewModel().importCourse(importParams, importOption, currentSchedule, it)
            }
        }
    }

    private fun afterSaveCourse(hasConflict: Boolean) {
        requireViewBinding().layoutWebCourseProvider.showShortSnackBar(R.string.course_import_success)
        if (hasConflict) {
            ImportCourseConflictDialog.showDialog(supportFragmentManager)
        }
    }

    private fun clearAll() {
        requireViewBinding().webViewWebCourseProvider.apply {
            clearHistory()
            clearFormData()
            clearMatches()
            clearSslPreferences()
            clearCache(true)
        }
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
        WebStorage.getInstance().deleteAllData()
    }

    private fun showCourseAdapterError(exception: CourseAdapterException) {
        ViewUtils.showCourseAdapterError(this, requireViewBinding().layoutWebCourseProvider, exception)
    }

    @Keep
    @Suppress("unused")
    private interface HTMLPrinterJavaScriptInterface {
        @Keep
        fun printHTML(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>, currentSchedule: Boolean)
    }
}