package tool.xfy9326.schedule.ui.fragment.base

import android.os.Bundle
import android.view.Gravity
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.transition.Slide

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
            requireActivity().actionBar?.setTitle(it)
        }
    }

    protected open fun onPrefInit(savedInstanceState: Bundle?) {}
}