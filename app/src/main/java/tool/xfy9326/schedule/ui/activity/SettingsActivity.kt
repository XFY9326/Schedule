package tool.xfy9326.schedule.ui.activity

import android.os.Bundle
import android.view.MenuItem
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivitySettingsBinding
import tool.xfy9326.schedule.ui.activity.base.ViewBindingActivity
import tool.xfy9326.schedule.ui.fragment.MainSettingsFragment

class SettingsActivity : ViewBindingActivity<ActivitySettingsBinding>() {

    override fun onInitView(viewBinding: ActivitySettingsBinding) {
        setSupportActionBar(viewBinding.toolBarSettings.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: ActivitySettingsBinding) {
        setupBaseSettingsFragment(bundle)
    }

    private fun setupBaseSettingsFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer_settingsContent, MainSettingsFragment())
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onRequestBack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onRequestBack() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }
}