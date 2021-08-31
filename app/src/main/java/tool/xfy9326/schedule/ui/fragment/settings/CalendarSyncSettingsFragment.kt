package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import lib.xfy9326.android.kit.show
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.dialog.MultiItemSelectDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel
import tool.xfy9326.schedule.utils.PermissionUtils

class CalendarSyncSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private const val TAG_CALENDAR_SYNC_LIST = "CALENDAR_SYNC_LIST"
        private const val TAG_CALENDAR_EDITABLE_LIST = "CALENDAR_EDITABLE_LIST"
        private const val TAG_CALENDAR_VISIBLE_LIST = "CALENDAR_VISIBLE_LIST"
        private const val TAG_CALENDAR_ADD_REMINDER_LIST = "CALENDAR_ADD_REMINDER_LIST"
    }

    override val titleName: Int = R.string.calendar_sync
    override val preferenceResId: Int = R.xml.settings_calendar_sync
    override val preferenceDataStore: PreferenceDataStore = AppSettingsDataStore.getPreferenceDataStore(lifecycleScope)
    private val requestCalendarPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (PermissionUtils.checkGrantResults(it)) {
            requireSettingsViewModel()?.syncToCalendar()
        } else {
            requireRootLayout()?.showSnackBar(R.string.calendar_permission_get_failed)
        }
    }

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(R.string.pref_sync_to_calendar) {
            lifecycleScope.launch {
                if (PermissionUtils.checkCalendarPermission(requireContext(), requestCalendarPermission)) {
                    requireSettingsViewModel()?.syncToCalendar()
                }
            }
        }
        setOnPrefClickListener(R.string.pref_clear_calendar) {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.clear_calendar)
                setMessage(R.string.clear_calendar_msg)
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    requireSettingsViewModel()?.clearCalendar()
                    requireRootLayout()?.showSnackBar(R.string.clear_calendar_success)
                }
            }.show(viewLifecycleOwner)
        }
        setOnPrefClickListener(R.string.pref_clear_sync_settings) {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.clear_calendar_settings)
                setMessage(R.string.clear_calendar_settings_msg)
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    requireSettingsViewModel()?.clearCalendarSettings()
                    requireRootLayout()?.showSnackBar(R.string.clear_calendar_settings_success)
                }
            }.show(viewLifecycleOwner)
        }
        setOnPrefClickListener(R.string.pref_calendar_sync_list) {
            requireSettingsViewModel()?.getScheduleSyncEditList(TAG_CALENDAR_SYNC_LIST)
        }
        setOnPrefClickListener(R.string.pref_calendar_editable_list) {
            requireSettingsViewModel()?.getScheduleSyncEditList(TAG_CALENDAR_EDITABLE_LIST)
        }
        setOnPrefClickListener(R.string.pref_calendar_visible_list) {
            requireSettingsViewModel()?.getScheduleSyncEditList(TAG_CALENDAR_VISIBLE_LIST)
        }
        setOnPrefClickListener(R.string.pref_calendar_add_reminder_list) {
            requireSettingsViewModel()?.getScheduleSyncEditList(TAG_CALENDAR_ADD_REMINDER_LIST)
        }
    }

    override fun onBindLiveDataFromSettingsViewMode(viewModel: SettingsViewModel) {
        viewModel.syncToCalendarStatus.observeEvent(viewLifecycleOwner) {
            if (it.success) {
                if (it.failedAmount == 0) {
                    requireRootLayout()?.showSnackBar(R.string.calendar_sync_success)
                } else {
                    requireRootLayout()?.showSnackBar(R.string.calendar_sync_failed, it.total, it.failedAmount)
                }
            } else {
                requireRootLayout()?.showSnackBar(R.string.calendar_sync_error)
            }
        }
        viewModel.scheduleSyncEdit.observeEvent(viewLifecycleOwner) {
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
                selectedArr = it.second.mapNotNull { pair ->
                    when (it.first) {
                        TAG_CALENDAR_SYNC_LIST -> pair.second.syncable
                        TAG_CALENDAR_EDITABLE_LIST -> pair.second.editable
                        TAG_CALENDAR_VISIBLE_LIST -> pair.second.defaultVisible
                        TAG_CALENDAR_ADD_REMINDER_LIST -> pair.second.addReminder
                        else -> null
                    }
                }.toBooleanArray()
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MultiItemSelectDialog.setOnMultiItemSelectedListener(childFragmentManager, viewLifecycleOwner) { tag, idArr, selectedArr ->
            requireSettingsViewModel()?.apply {
                when (tag) {
                    TAG_CALENDAR_SYNC_LIST -> updateSyncEnabled(idArr, selectedArr)
                    TAG_CALENDAR_EDITABLE_LIST -> updateSyncEditable(idArr, selectedArr)
                    TAG_CALENDAR_VISIBLE_LIST -> updateSyncVisible(idArr, selectedArr)
                    TAG_CALENDAR_ADD_REMINDER_LIST -> updateSyncReminder(idArr, selectedArr)
                }
            }
        }
    }
}