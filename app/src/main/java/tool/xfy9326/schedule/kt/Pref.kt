package tool.xfy9326.schedule.kt

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

fun <T : Preference> PreferenceFragmentCompat.findPreference(@StringRes keyId: Int): T? = findPreference(getString(keyId))

fun PreferenceFragmentCompat.setOnPrefClickListener(@StringRes keyId: Int, action: (Preference) -> Unit) {
    findPreference<Preference>(keyId)?.setOnPreferenceClickListener {
        action(it)
        false
    }
}

inline fun <reified T : Fragment> PreferenceFragmentCompat.bindPrefFragment(@StringRes keyId: Int) {
    findPreference<Preference>(keyId)?.fragment = T::class.qualifiedName
}