package tool.xfy9326.schedule.ui.fragment.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.schedule.kt.consumeSystemBarInsets
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
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferenceDataStore
        setPreferencesFromResource(preferenceResId, rootKey)
        onPrefInit(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return super.onCreateView(inflater, container, savedInstanceState).also {
            it.setBackgroundColor(requireContext().getDefaultBackgroundColor())
        }
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireSettingsViewModel()?.let(::onBindLiveDataFromSettingsViewMode)
        view.findViewById<RecyclerView>(androidx.preference.R.id.recycler_view)?.consumeSystemBarInsets(bottom = true)
    }

    override fun onStart() {
        super.onStart()
        titleName?.let {
            (requireActivity() as? AppCompatActivity)?.supportActionBar?.setTitle(it)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun getCallbackFragment() = parentFragment

    protected open fun onBindLiveDataFromSettingsViewMode(viewModel: SettingsViewModel) {}

    protected open fun onPrefInit(savedInstanceState: Bundle?) {}

    protected fun requireSettingsViewModel() = (activity as? SettingsActivity)?.settingsViewModel

    protected fun requireRootLayout() = (activity as AbstractSettingsActivity?)?.rootLayout
}