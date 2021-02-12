package tool.xfy9326.schedule.ui.fragment.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import tool.xfy9326.schedule.kt.getDefaultBackgroundColor
import tool.xfy9326.schedule.ui.activity.SettingsActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractSettingsActivity
import tool.xfy9326.schedule.ui.vm.SettingsViewModel

abstract class AbstractSettingsFragment : PreferenceFragmentCompat() {
    protected abstract val preferenceResId: Int
    protected open val preferenceDataStore: PreferenceDataStore? = null

    @StringRes
    protected open val titleName: Int? = null

    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireSettingsViewModel()?.let(::onBindLiveDataFromSettingsViewMode)
    }

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferenceDataStore
        setPreferencesFromResource(preferenceResId, rootKey)
        onPrefInit(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.also {
            it.setBackgroundColor(requireContext().getDefaultBackgroundColor())
        }
    }

    override fun onStart() {
        super.onStart()
        titleName?.let {
            (requireActivity() as? AppCompatActivity)?.supportActionBar?.setTitle(it)
        }
    }

    protected open fun onBindLiveDataFromSettingsViewMode(viewModel: SettingsViewModel) {}

    protected open fun onPrefInit(savedInstanceState: Bundle?) {}

    protected fun requireSettingsViewModel() = (activity as? SettingsActivity)?.settingsViewModel

    protected fun requireRootLayout() = (activity as AbstractSettingsActivity?)?.rootLayout

    protected fun requestBack() = (activity as AbstractSettingsActivity?)?.requestBack()
}