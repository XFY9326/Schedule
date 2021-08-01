@file:Suppress("unused")

package lib.xfy9326.android.kit

import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

fun WebView.bindLifeCycle(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            onResume()
        }

        override fun onPause(owner: LifecycleOwner) {
            onPause()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            try {
                removeSelf()
                stopLoading()
                settings.javaScriptEnabled = false
                removeAllViews()
                destroy()
            } catch (e: Exception) {
                // Ignore
            }
            lifecycleOwner.lifecycle.removeObserver(this)
        }
    })
}

fun WebView.clearAll(cookies: Boolean = true, webStorage: Boolean = true) {
    settings.javaScriptEnabled = false
    clearHistory()
    clearFormData()
    clearMatches()
    clearSslPreferences()
    clearCache(true)
    if (cookies) {
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
    }
    if (webStorage) {
        WebStorage.getInstance().deleteAllData()
    }
}