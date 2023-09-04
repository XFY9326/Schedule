package tool.xfy9326.schedule.ui.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStateAtLeast
import io.github.xfy9326.atools.core.startActivity
import io.github.xfy9326.atools.ui.bindLifeCycle
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityWebBinding
import tool.xfy9326.schedule.ui.activity.base.ViewBindingActivity
import tool.xfy9326.schedule.utils.AppUriUtils
import tool.xfy9326.schedule.utils.consumeSystemBarInsets

class WebActivity : ViewBindingActivity<ActivityWebBinding>() {
    companion object {
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_URL = "EXTRA_URL"
        const val EXTRA_UA = "EXTRA_UA"
        const val EXTRA_NO_CACHE = "EXTRA_NO_CACHE"
        private const val EXTRA_WEB_VIEW = "EXTRA_WEB_VIEW"
    }

    override fun onContentViewPreload(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onCreateViewBinding(): ActivityWebBinding = ActivityWebBinding.inflate(layoutInflater)

    override fun onBindView(viewBinding: ActivityWebBinding) {
        super.onBindView(viewBinding)
        setSupportActionBar(viewBinding.toolBarWeb.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        intent?.extras?.getString(EXTRA_TITLE)?.let { supportActionBar?.title = it }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onInitView(viewBinding: ActivityWebBinding) {
        viewBinding.webViewWeb.apply {
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
                intent?.extras?.getString(EXTRA_UA)?.let {
                    userAgentString = it
                }
                if (intent?.extras?.getBoolean(EXTRA_NO_CACHE, false) == true) {
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    changeProgressBar(newProgress)
                }
            }
            webViewClient = object : WebViewClient() {
                @Suppress("OVERRIDE_DEPRECATION")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return if (url != null) {
                        try {
                            url.toUri()
                        } catch (_: Exception) {
                            null
                        }?.let(::handleUrl) ?: false
                    } else false
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return if (request != null) handleUrl(request.url) else false
                }
            }
            bindLifeCycle(this@WebActivity)
        }
        viewBinding.toolBarWeb.toolBarGeneral.consumeSystemBarInsets()
        viewBinding.webViewWeb.consumeSystemBarInsets()
    }

    override fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: ActivityWebBinding) {
        if (bundle == null) {
            intent?.extras?.getString(EXTRA_URL)?.let {
                viewBinding.webViewWeb.loadUrl(it)
            }
        } else {
            bundle.getBundle(EXTRA_WEB_VIEW)?.let {
                viewBinding.webViewWeb.restoreState(it)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putBundle(EXTRA_WEB_VIEW, Bundle().apply {
            requireViewBinding().webViewWeb.saveState(this)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_webview, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_webViewRefresh -> requireViewBinding().webViewWeb.reload()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleUrl(url: Uri): Boolean {
        if (AppUriUtils.isJSCourseImportUri(url)) {
            startActivity<OnlineCourseImportActivity> {
                data = url
            }
            finish()
            return true
        }
        return false
    }

    private fun changeProgressBar(newProgress: Int) {
        lifecycleScope.launch {
            withStateAtLeast(Lifecycle.State.STARTED) {
                requireViewBinding().progressBarWeb.apply {
                    visibility = if (newProgress != 100) View.VISIBLE else View.INVISIBLE
                    progress = newProgress
                }
            }
        }
    }
}