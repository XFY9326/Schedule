package tool.xfy9326.schedule.ui.fragment

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.show
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.tools.livedata.observeEvent
import tool.xfy9326.schedule.ui.dialog.MultiItemSelectDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel
import tool.xfy9326.schedule.utils.PermissionUtils

@Suppress("unused")
class CalendarSyncSettingsFragment : AbstractSettingsFragment(), MultiItemSelectDialog.OnMultiItemSelectedListener {
    companion object {
        private const val PREFERENCE_SYNC_TO_CALENDAR = "syncToCalendar"
        private const val PREFERENCE_CLEAR_CALENDAR = "clearCalendar"
        private const val PREFERENCE_CLEAR_SYNC_SETTINGS = "clearSyncSettings"

        private const val PREFERENCE_CALENDAR_SYNC_LIST = "calendarSyncList"
        private const val PREFERENCE_CALENDAR_EDITABLE_LIST = "calendarEditableList"
        private const val PREFERENCE_CALENDAR_VISIBLE_LIST = "calendarVisibleList"
        private const val PREFERENCE_CALENDAR_ADD_REMINDER_LIST = "calendarSyncAddReminderList"

        private const val REQUEST_CODE_CALENDAR_PERMISSION = 1
    }

    override val titleName: Int = R.string.calendar_sync_settings
    override val preferenceResId: Int = R.xml.settings_calendar_sync
    override val preferenceDataStore: PreferenceDataStore = AppSettingsDataStore.getPreferenceDataStore(lifecycleScope)
    private val requestCalendarPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (PermissionUtils.checkGrantResults(it)) {
            requireSettingsViewModel()?.syncToCalendar()
        } else {
            requireRootLayout()?.showShortSnackBar(R.string.calendar_permission_get_failed)
        }
    }

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(PREFERENCE_SYNC_TO_CALENDAR) {
            lifecycleScope.launch {
                if (PermissionUtils.checkCalendarPermission(requireContext(), requestCalendarPermission)) {
                    requireSettingsViewModel()?.syncToCalendar()
                }
            }
        }
        setOnPrefClickListener(PREFERENCE_CLEAR_CALENDAR) {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.clear_calendar)
                setMessage(R.string.clear_calendar_msg)
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    requireSettingsViewModel()?.clearCalendar()
                    requireRootLayout()?.showShortSnackBar(R.string.clear_calendar_success)
                }
            }.show(viewLifecycleOwner)
        }
        setOnPrefClickListener(PREFERENCE_CLEAR_SYNC_SETTINGS) {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.clear_calendar_settings)
                setMessage(R.string.clear_calendar_settings_msg)
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    requireSettingsViewModel()?.clearCalendarSettings()
                    requireRootLayout()?.showShortSnackBar(R.string.clear_calendar_settings_success)
                }
            }.show(viewLifecycleOwner)
        }
        setOnPrefClickListener(PREFERENCE_CALENDAR_SYNC_LIST) {
            requireSettingsViewModel()?.getScheduleSyncEditList(PREFERENCE_CALENDAR_SYNC_LIST)
        }
        setOnPrefClickListener(PREFERENCE_CALENDAR_EDITABLE_LIST) {
            requireSettingsViewModel()?.getScheduleSyncEditList(PREFERENCE_CALENDAR_EDITABLE_LIST)
        }
        setOnPrefClickListener(PREFERENCE_CALENDAR_VISIBLE_LIST) {
            requireSettingsViewModel()?.getScheduleSyncEditList(PREFERENCE_CALENDAR_VISIBLE_LIST)
        }
        setOnPrefClickListener(PREFERENCE_CALENDAR_ADD_REMINDER_LIST) {
            requireSettingsViewModel()?.getScheduleSyncEditList(PREFERENCE_CALENDAR_ADD_REMINDER_LIST)
        }
    }

    override fun onBindLiveDataFromSettingsViewMode(viewModel: SettingsViewModel) {
        viewModel.syncToCalendarStatus.observeEvent(this) {
            if (it.success) {
                if (it.failedAmount == 0) {
                    requireRootLayout()?.showShortSnackBar(R.string.calendar_sync_success)
                } else {
                    requireRootLayout()?.showShortSnackBar(R.string.calendar_sync_failed, it.total, it.failedAmount)
                }
            } else {
                requireRootLayout()?.showShortSnackBar(R.string.calendar_sync_error)
            }
        }
        viewModel.scheduleSyncEdit.observeEvent(this) {
            MultiItemSelectDialog.showDialog(
                childFragmentManager,
                it.first,
                getString(R.string.select_multi_schedule),
                showArr = it.second.map { pair ->
                    pair.first.name
                }.toTypedArray(),
                idArr = it.second.map { pair ->
                    pair.first.scheduleId
                }.toLongArray(),
                selectedArr = when (it.first) {
                    PREFERENCE_CALENDAR_SYNC_LIST -> it.second.map { pair ->
                        pair.second.syncable
                    }
                    PREFERENCE_CALENDAR_EDITABLE_LIST -> it.second.map { pair ->
                        pair.second.editable
                    }
                    PREFERENCE_CALENDAR_VISIBLE_LIST -> it.second.map { pair ->
                        pair.second.defaultVisible
                    }
                    PREFERENCE_CALENDAR_ADD_REMINDER_LIST -> it.second.map { pair ->
                        pair.second.addReminder
                    }
                    else -> error("Unsupported key! ${it.first}")
                }.toBooleanArray()
            )
        }
    }

    override fun onMultiItemSelected(tag: String?, idArr: LongArray, selectedArr: BooleanArray) {
        requireSettingsViewModel()?.apply {
            when (tag) {
                PREFERENCE_CALENDAR_SYNC_LIST -> updateSyncEnabled(idArr, selectedArr)
                PREFERENCE_CALENDAR_EDITABLE_LIST -> updateSyncEditable(idArr, selectedArr)
                PREFERENCE_CALENDAR_VISIBLE_LIST -> updateSyncVisible(idArr, selectedArr)
                PREFERENCE_CALENDAR_ADD_REMINDER_LIST -> updateSyncReminder(idArr, selectedArr)
            }
        }
    }
}