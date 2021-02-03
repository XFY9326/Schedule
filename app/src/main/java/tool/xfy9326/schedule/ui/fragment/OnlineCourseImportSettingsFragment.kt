package tool.xfy9326.schedule.ui.fragment

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

@Suppress("unused")
class OnlineCourseImportSettingsFragment : AbstractSettingsFragment() {
    override val titleName: Int = R.string.online_course_import
    override val preferenceResId: Int = R.xml.settings_online_course_import
    override val preferenceDataStore: PreferenceDataStore = AppSettingsDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        findPreference<CheckBoxPreference>(AppSettingsDataStore.enableOnlineCourseImport.name)?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == false) {
                lifecycleScope.launch { AppDataStore.setReadOnlineImportAttention(false) }
            }
            true
        }
    }
}