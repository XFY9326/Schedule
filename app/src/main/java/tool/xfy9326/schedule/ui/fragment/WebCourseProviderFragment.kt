package tool.xfy9326.schedule.ui.fragment

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.webkit.*
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.databinding.FragmentWebCourseProviderBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.ui.dialog.WebCourseProviderBottomPanel
import tool.xfy9326.schedule.ui.fragment.base.IWebCourseProvider
import tool.xfy9326.schedule.ui.fragment.base.ViewBindingFragment
import tool.xfy9326.schedule.utils.JSBridge
import java.lang.ref.WeakReference

class WebCourseProviderFragment : ViewBindingFragment<FragmentWebCourseProviderBinding>(), WebCourseProviderBottomPanel.BottomPanelActionListener,
    IWebCourseProvider.IFragmentContact {
    companion object {
        const val EXTRA_INIT_PAGE_URL = "EXTRA_INIT_PAGE_URL"
        const val EXTRA_AUTHOR_NAME = "EXTRA_AUTHOR_NAME"

        private const val EXTRA_WEB_VIEW = "EXTRA_WEB_VIEW"
        private const val EXTRA_IS_BOTTOM_PANEL_INIT_SHOWED = "EXTRA_IS_BOTTOM_PANEL_INIT_SHOWED"
    }

    private var isBottomPanelInitShowed = false
    private val hideBottomPanelAnimation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_bottom_button_out) }
    private val showBottomPanelAnimation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_bottom_button_in) }

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentWebCourseProviderBinding.inflate(inflater, container, false)

    override fun onBindViewBinding(view: View) = FragmentWebCourseProviderBinding.bind(view)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onInitView(viewBinding: FragmentWebCourseProviderBinding) {
        setHasOptionsMenu(true)

        val enableDebug = runBlocking {
            if (!AppSettingsDataStore.keepWebProviderCacheFlow.first()) {
                viewBinding.webViewWebCourseProvider.clearAll()
            }
            AppSettingsDataStore.enableWebCourseProviderConsoleDebugFlow.first().also {
                WebView.setWebContentsDebuggingEnabled(it)
            }
        }

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

                override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    if (message != null && result != null) {
                        showToast(message)
                        result.confirm()
                        return true
                    }
                    return super.onJsAlert(view, url, message, result)
                }

                override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    if (message != null && result != null) {
                        showJSConfirmDialog(message, result)
                        return true
                    }
                    return super.onJsConfirm(view, url, message, result)
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    handler?.proceed()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    if (enableDebug) {
                        view?.evaluateJavascript(JSBridge.V_CONSOLE_INJECT, null)
                    }
                    super.onPageFinished(view, url)
                }
            }
            requireOwner<IWebCourseProvider.IActivityContact>()?.onSetupWebView(this)
            bindLifeCycle(this@WebCourseProviderFragment)
        }

        viewBinding.buttonWebCourseProviderPanel.setOnClickListener {
            showBottomPanel()
        }
    }

    override fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: FragmentWebCourseProviderBinding) {
        if (bundle == null) {
            requireArguments().getString(EXTRA_INIT_PAGE_URL)?.let {
                viewBinding.webViewWebCourseProvider.loadUrl(it)
            }
        } else {
            if (WebCourseProviderBottomPanel.isShowing(childFragmentManager)) {
                viewBinding.buttonWebCourseProviderPanel.isVisible = false
            }
            bundle.getBundle(EXTRA_WEB_VIEW)?.let {
                viewBinding.webViewWebCourseProvider.restoreState(it)
            }
        }

        isBottomPanelInitShowed = bundle?.getBoolean(EXTRA_IS_BOTTOM_PANEL_INIT_SHOWED, false) ?: false
        if (!isBottomPanelInitShowed) {
            showBottomPanel()
            isBottomPanelInitShowed = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_IS_BOTTOM_PANEL_INIT_SHOWED, isBottomPanelInitShowed)
        outState.putBundle(EXTRA_WEB_VIEW, Bundle().apply {
            requireViewBinding().webViewWebCourseProvider.saveState(this)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_web_course_provider, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_webCourseProviderRefresh) {
            requireViewBinding().webViewWebCourseProvider.reload()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showJSConfirmDialog(msg: String, jsResult: JsResult) {
        val weakRef = WeakReference(jsResult)
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.js_confirm_title)
            setMessage(msg)
            setPositiveButton(android.R.string.ok) { _, _ ->
                weakRef.get()?.confirm()
            }
            setNegativeButton(android.R.string.cancel, null)
            setOnDismissListener {
                weakRef.get()?.cancel()
            }
            setCancelable(false)
        }.show(this)
    }

    private fun changeProgressBar(newProgress: Int) {
        if (isAdded) {
            requireViewBinding().progressBarWebCourseProvider.apply {
                visibility = if (newProgress != 100) View.VISIBLE else View.INVISIBLE
                progress = newProgress
            }
        }
    }

    private fun showBottomPanel() {
        if (isAdded) {
            requireViewBinding().buttonWebCourseProviderPanel.apply {
                isVisible = false
                startAnimation(hideBottomPanelAnimation)
            }
            WebCourseProviderBottomPanel.showDialog(childFragmentManager, requireArguments().getString(EXTRA_AUTHOR_NAME).orEmpty())
        }
    }

    override fun onImportCourseToSchedule(isCurrentSchedule: Boolean) {
        requireOwner<IWebCourseProvider.IActivityContact>()?.onImportCourseToSchedule(isCurrentSchedule)
    }

    override fun onBottomPanelDismiss() {
        if (isAdded) {
            requireViewBinding().buttonWebCourseProviderPanel.apply {
                isVisible = true
                startAnimation(showBottomPanelAnimation)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (isAdded) {
            requireViewBinding().webViewWebCourseProvider.apply {
                if (canGoBack()) {
                    goBack()
                    return true
                }
            }
        }
        return false
    }

    override fun evaluateJavascript(content: String, callback: ((String) -> Unit)?) {
        if (isAdded) {
            requireViewBinding().webViewWebCourseProvider.evaluateJavascript(content, callback)
        }
    }

    override fun refresh() {
        requireViewBinding().webViewWebCourseProvider.reload()
    }
}