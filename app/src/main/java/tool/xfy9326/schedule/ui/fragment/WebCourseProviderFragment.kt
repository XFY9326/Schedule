package tool.xfy9326.schedule.ui.fragment

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.webkit.*
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.databinding.FragmentWebCourseProviderBinding
import tool.xfy9326.schedule.kt.bindLifeCycle
import tool.xfy9326.schedule.kt.clearAll
import tool.xfy9326.schedule.kt.requireOwner
import tool.xfy9326.schedule.ui.dialog.WebCourseProviderBottomPanel
import tool.xfy9326.schedule.ui.fragment.base.ViewBindingFragment

class WebCourseProviderFragment : ViewBindingFragment<FragmentWebCourseProviderBinding>(), WebCourseProviderBottomPanel.BottomPanelActionListener {
    companion object {
        const val EXTRA_INIT_PAGE_URL = "EXTRA_INIT_PAGE_URL"
        const val EXTRA_AUTHOR_NAME = "EXTRA_AUTHOR_NAME"

        private const val EXTRA_WEB_VIEW = "EXTRA_WEB_VIEW"
        private const val EXTRA_IS_BOTTOM_PANEL_INIT_SHOWED = "EXTRA_IS_BOTTOM_PANEL_INIT_SHOWED"

        interface IActivityContact {
            fun onSetupWebView(webView: WebView)

            fun onImportCourseToSchedule(isCurrentSchedule: Boolean)
        }
    }

    private var isBottomPanelInitShowed = false
    private val hideBottomPanelAnimation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_bottom_button_out) }
    private val showBottomPanelAnimation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_bottom_button_in) }

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentWebCourseProviderBinding.inflate(inflater, container, false)

    override fun onBindViewBinding(view: View) = FragmentWebCourseProviderBinding.bind(view)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onInitView(viewBinding: FragmentWebCourseProviderBinding) {
        setHasOptionsMenu(true)

        lifecycleScope.launch {
            if (!AppSettingsDataStore.keepWebProviderCacheFlow.first()) {
                viewBinding.webViewWebCourseProvider.clearAll()
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
            }
            webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    handler?.proceed()
                }
            }
            requireOwner<IActivityContact>()?.onSetupWebView(this)
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
        requireOwner<IActivityContact>()?.onImportCourseToSchedule(isCurrentSchedule)
    }

    override fun onBottomPanelDismiss() {
        if (isAdded) {
            requireViewBinding().buttonWebCourseProviderPanel.apply {
                isVisible = true
                startAnimation(showBottomPanelAnimation)
            }
        }
    }

    fun onBackPressed(): Boolean {
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

    fun evaluateJavascript(content: String, callback: ((String) -> Unit)? = null) {
        if (isAdded) {
            requireViewBinding().webViewWebCourseProvider.evaluateJavascript(content, callback)
        }
    }
}