@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.ui.activity.base

import android.os.Bundle
import android.view.MenuItem
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivitySettingsBinding
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

abstract class AbstractSettingsActivity : ViewBindingActivity<ActivitySettingsBinding>() {
    val rootLayout by lazy {
        requireViewBinding().layoutSettings
    }

    protected abstract fun onCreateMainSettingsFragment(): AbstractSettingsFragment

    override fun onCreateViewBinding() = ActivitySettingsBinding.inflate(layoutInflater)

    override fun onInitView(viewBinding: ActivitySettingsBinding) {
        setSupportActionBar(viewBinding.toolBarSettings.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: ActivitySettingsBinding) {
        setupBaseSettingsFragment(bundle)
    }

    private fun setupBaseSettingsFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer_settingsContent, onCreateMainSettingsFragment()).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            requestBack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun requestBack() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }
}