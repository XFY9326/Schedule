@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.ui.activity.base

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityFragmentContainerBinding
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

abstract class AbstractSettingsActivity : ViewBindingActivity<ActivityFragmentContainerBinding>(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    val rootLayout by lazy {
        requireViewBinding().layoutFragmentContainer
    }

    protected abstract fun onCreateMainSettingsFragment(): AbstractSettingsFragment

    override fun onCreateViewBinding() = ActivityFragmentContainerBinding.inflate(layoutInflater)

    override fun onInitView(viewBinding: ActivityFragmentContainerBinding) {
        setSupportActionBar(viewBinding.toolBarFragmentContainer.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: ActivityFragmentContainerBinding) {
        setupBaseSettingsFragment(bundle)
    }

    private fun setupBaseSettingsFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, onCreateMainSettingsFragment())
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            requestBack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Reference: [PreferenceFragmentCompat.onPreferenceTreeClick]
     * Deprecated API usage: [androidx.fragment.app.Fragment.setTargetFragment]
     */
    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment)
        fragment.arguments = pref.extras

        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.anim_scroll_in_right,
                R.anim.anim_scroll_out_left,
                R.anim.anim_scroll_in_left,
                R.anim.anim_scroll_out_right
            )
            replace(R.id.fragmentContainer, fragment)
            addToBackStack(null)
        }
        return true
    }

    fun requestBack() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }
}