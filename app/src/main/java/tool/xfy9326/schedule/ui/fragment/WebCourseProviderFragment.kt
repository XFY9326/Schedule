package tool.xfy9326.schedule.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.webkit.*
import androidx.activity.addCallback
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStateAtLeast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xfy9326.atools.core.showToast
import io.github.xfy9326.atools.ui.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.databinding.FragmentWebCourseProviderBinding
import tool.xfy9326.schedule.ui.dialog.WebCourseProviderBottomPanel
import tool.xfy9326.schedule.ui.fragment.base.IWebCourseProvider
import tool.xfy9326.schedule.ui.fragment.base.ViewBindingFragment
import tool.xfy9326.schedule.utils.JSBridge
import java.lang.ref.WeakReference

class WebCourseProviderFragment : ViewBindingFragment<FragmentWebCourseProviderBinding>(), IWebCourseProvider.IFragmentContact {
    companion object {
        const val EXTRA_INIT_PAGE_URL = "EXTRA_INIT_PAGE_URL"
        const val EXTRA_AUTHOR_NAME = "EXTRA_AUTHOR_NAME"

        private const val EXTRA_WEB_VIEW = "EXTRA_WEB_VIEW"
        private const val EXTRA_IS_BOTTOM_PANEL_INIT_SHOWED = "EXTRA_IS_BOTTOM_PANEL_INIT_SHOWED"
        private const val EXTRA_IS_WEB_VIEW_CONNECTION_ENABLED = "EXTRA_IS_WEB_VIEW_CONNECTION_ENABLED"
    }

    private var isWebViewConnectionEnabled = true
    private var isWebViewDebugLogEnabled = BuildConfig.DEBUG
    private var isBottomPanelInitShowed = false
    private val hideBottomPanelAnimation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_bottom_button_out) }
    private val showBottomPanelAnimation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_bottom_button_in) }

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentWebCourseProviderBinding.inflate(inflater, container, false)

    override fun onBindViewBinding(view: View) = FragmentWebCourseProviderBinding.bind(view)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onInitView(viewBinding: FragmentWebCourseProviderBinding) {
        addMenuInActivity()
        addRequestBackCallback()

        val enableDebug = runBlocking {
            if (!AppSettingsDataStore.keepWebProviderCacheFlow.first()) {
                viewBinding.webViewWebCourseProvider.clearAll()
            }
            AppSettingsDataStore.enableWebCourseProviderConsoleDebugFlow.first().also {
                WebView.setWebContentsDebuggingEnabled(it)
                isWebViewDebugLogEnabled = it
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
                blockNetworkLoads = !isWebViewConnectionEnabled
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    changeProgressBar(newProgress)
                }

                override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                    if (message != null && result != null) {
                        requireContext().showToast(message)
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

                @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
                override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
                    if (isWebViewDebugLogEnabled) {
                        super.onConsoleMessage(message, lineNumber, sourceID)
                    }
                }

                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    return if (isWebViewDebugLogEnabled) {
                        super.onConsoleMessage(consoleMessage)
                    } else {
                        true
                    }
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    if (enableDebug) {
                        view?.evaluateJavascript(JSBridge.V_CONSOLE_INJECT, null)
                    }
                    super.onPageFinished(view, url)
                }

                @Suppress("OVERRIDE_DEPRECATION")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return false
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }
            }
            requireOwner<IWebCourseProvider.IActivityContact>()?.onSetupWebView(this)
            bindLifeCycle(this@WebCourseProviderFragment)
        }

        viewBinding.buttonWebCourseProviderPanel.setOnClickListener {
            showBottomPanel()
        }

        WebCourseProviderBottomPanel.setBottomPanelActionListener(childFragmentManager, viewLifecycleOwner,
            onDismiss = {
                lifecycleScope.launch {
                    withStateAtLeast(Lifecycle.State.STARTED) {
                        requireViewBinding().buttonWebCourseProviderPanel.apply {
                            isVisible = true
                            startAnimation(showBottomPanelAnimation)
                        }
                    }
                }
            },
            onImport = {
                requireOwner<IWebCourseProvider.IActivityContact>()?.onImportCourseToSchedule(it)
            }
        )
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
        isWebViewConnectionEnabled = bundle?.getBoolean(EXTRA_IS_WEB_VIEW_CONNECTION_ENABLED, true) ?: true
        if (!isBottomPanelInitShowed) {
            showBottomPanel()
            isBottomPanelInitShowed = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_IS_BOTTOM_PANEL_INIT_SHOWED, isBottomPanelInitShowed)
        outState.putBoolean(EXTRA_IS_WEB_VIEW_CONNECTION_ENABLED, isWebViewConnectionEnabled)
        outState.putBundle(EXTRA_WEB_VIEW, Bundle().apply {
            requireViewBinding().webViewWebCourseProvider.saveState(this)
        })
    }

    private fun addMenuInActivity() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_web_course_provider, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.menu_webCourseProviderRefresh) {
                    requireViewBinding().webViewWebCourseProvider.reload()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)
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
        }.show(viewLifecycleOwner)
    }

    private fun changeProgressBar(newProgress: Int) {
        lifecycleScope.launch {
            withStateAtLeast(Lifecycle.State.STARTED) {
                requireViewBinding().progressBarWebCourseProvider.apply {
                    visibility = if (newProgress != 100) View.VISIBLE else View.INVISIBLE
                    progress = newProgress
                }
            }
        }
    }

    private fun showBottomPanel() {
        lifecycleScope.launch {
            withStateAtLeast(Lifecycle.State.STARTED) {
                requireViewBinding().buttonWebCourseProviderPanel.apply {
                    isVisible = false
                    startAnimation(hideBottomPanelAnimation)
                }
                WebCourseProviderBottomPanel.showDialog(childFragmentManager, requireArguments().getString(EXTRA_AUTHOR_NAME).orEmpty())
            }
        }
    }

    private fun addRequestBackCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireViewBinding().webViewWebCourseProvider.apply {
                if (canGoBack()) {
                    goBack()
                } else {
                    resume(requireActivity().onBackPressedDispatcher)
                }
            }
        }
    }

    override fun evaluateJavascript(content: String, callback: ((String?) -> Unit)?) {
        lifecycleScope.launch {
            withStateAtLeast(Lifecycle.State.STARTED) {
                requireViewBinding().webViewWebCourseProvider.evaluateJavascript(content, callback)
            }
        }
    }

    override fun refresh() {
        lifecycleScope.launch {
            withStateAtLeast(Lifecycle.State.STARTED) {
                requireViewBinding().webViewWebCourseProvider.reload()
            }
        }
    }

    override fun setWebViewConnection(enabled: Boolean, autoRefresh: Boolean) {
        lifecycleScope.launch {
            withStateAtLeast(Lifecycle.State.STARTED) {
                isWebViewConnectionEnabled = enabled
                requireViewBinding().webViewWebCourseProvider.apply {
                    settings.blockNetworkLoads = !enabled
                    if (autoRefresh) {
                        reload()
                    }
                }
            }
        }
    }
}