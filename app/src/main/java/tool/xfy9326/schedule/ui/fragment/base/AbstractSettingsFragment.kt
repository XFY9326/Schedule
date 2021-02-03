package tool.xfy9326.schedule.ui.fragment.base

import android.os.Bundle
import android.view.Gravity
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.transition.Slide
import tool.xfy9326.schedule.ui.activity.SettingsActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractSettingsActivity

abstract class AbstractSettingsFragment : PreferenceFragmentCompat() {
    protected abstract val preferenceResId: Int
    protected open val preferenceDataStore: PreferenceDataStore? = null

    @StringRes
    protected open val titleName: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Slide(Gravity.END)
        exitTransition = Slide(Gravity.START)
    }

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferenceDataStore
        setPreferencesFromResource(preferenceResId, rootKey)
        onPrefInit(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        titleName?.let {
            (requireActivity() as? AppCompatActivity)?.supportActionBar?.setTitle(it)
        }
    }

    protected open fun onPrefInit(savedInstanceState: Bundle?) {}

    protected fun requireSettingsViewModel() = (activity as? SettingsActivity)?.settingsViewModel

    protected fun requireRootLayout() = (activity as AbstractSettingsActivity?)?.rootLayout

    protected fun requestBack() = (activity as AbstractSettingsActivity?)?.requestBack()
}