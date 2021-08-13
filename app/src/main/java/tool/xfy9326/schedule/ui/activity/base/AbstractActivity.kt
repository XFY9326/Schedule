package tool.xfy9326.schedule.ui.activity.base

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppSettingsDataStore
import kotlin.properties.Delegates

abstract class AbstractActivity : AppCompatActivity() {
    protected open var useBackInsteadOfNavigateHome: Boolean = true
    protected open var enableCustomActivityAnimation: Boolean = true

    private var internalIsFirstLaunch by Delegates.notNull<Boolean>()
    val isFirstLaunch: Boolean
        get() = internalIsFirstLaunch

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        internalIsFirstLaunch = savedInstanceState == null
        if (enableCustomActivityAnimation && AppSettingsDataStore.useCustomActivityTransitionAnimation) {
            window?.setWindowAnimations(R.style.AppTheme_ActivityAnimation)
        }
        super.onCreate(savedInstanceState)
        onActivityInit(savedInstanceState)
    }

    protected open fun onActivityInit(savedInstanceState: Bundle?) {}

    @CallSuper
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (useBackInsteadOfNavigateHome && item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}