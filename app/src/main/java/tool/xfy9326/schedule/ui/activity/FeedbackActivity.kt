package tool.xfy9326.schedule.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityFeedbackBinding
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.ui.activity.base.ViewBindingActivity
import tool.xfy9326.schedule.utils.IntentUtils

@SuppressLint("SetJavaScriptEnabled")
class FeedbackActivity : ViewBindingActivity<ActivityFeedbackBinding>() {
    companion object {
        private const val EXTRA_WEB_VIEW = "EXTRA_WEB_VIEW"
        private const val SCHEMA_WEIXIN = "weixin"
        private const val ONLINE_FEEDBACK_URL = "https://support.qq.com/product/301005?d-wx-push=1"
        private val CLIENT_DATA =
            "clientVersion=${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})&osVersion=${Build.VERSION.RELEASE}(${Build.VERSION.SDK_INT})&device=${Build.BRAND}-${Build.MODEL}".toByteArray()
    }

    override fun onInitView(viewBinding: ActivityFeedbackBinding) {
        setSupportActionBar(viewBinding.toolBarFeedback.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewBinding.webViewFeedback.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    changeProgressBar(newProgress)
                }
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    request?.let {
                        try {
                            val url = it.url
                            if (url != null && url.scheme == SCHEMA_WEIXIN) {
                                startActivity(Intent(Intent.ACTION_VIEW, url))
                                return true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return false
                        }
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
        }
    }

    override fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: ActivityFeedbackBinding) {
        if (bundle == null) {
            viewBinding.webViewFeedback.postUrl(ONLINE_FEEDBACK_URL, CLIENT_DATA)
        } else {
            bundle.getBundle(EXTRA_WEB_VIEW)?.let {
                viewBinding.webViewFeedback.restoreState(it)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(EXTRA_WEB_VIEW, buildBundle {
            requireViewBinding().webViewFeedback.saveState(this)
        })
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_feedback, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_feedbackRefresh -> requireViewBinding().webViewFeedback.reload()
            R.id.menu_feedbackOpenInBrowser -> IntentUtils.openUrlInBrowser(this, ONLINE_FEEDBACK_URL)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeProgressBar(newProgress: Int) {
        requireViewBinding().progressBarFeedback.apply {
            visibility = if (newProgress != 100) View.VISIBLE else View.INVISIBLE
            progress = newProgress
        }
    }

    override fun onBackPressed() {
        requireViewBinding().webViewFeedback.apply {
            if (canGoBack()) {
                goBack()
            } else {
                super.onBackPressed()
            }
        }
    }
}