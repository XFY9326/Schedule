package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.bindPrefFragment
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.ui.activity.AboutActivity
import tool.xfy9326.schedule.ui.activity.FeedbackActivity
import tool.xfy9326.schedule.ui.dialog.UpgradeDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.utils.UpgradeUtils

class MainSettingsFragment : AbstractSettingsFragment() {
    override val preferenceResId: Int = R.xml.settings_main
    override val titleName: Int = R.string.settings

    override fun onPrefInit(savedInstanceState: Bundle?) {
        bindPrefFragment<GeneralSettingsFragment>(R.string.pref_general_settings)
        bindPrefFragment<ScheduleSettingsFragment>(R.string.pref_schedule_table_settings)
        bindPrefFragment<CalendarSyncSettingsFragment>(R.string.pref_calendar_sync_settings)
        bindPrefFragment<OnlineCourseImportSettingsFragment>(R.string.pref_online_course_import_settings)
        bindPrefFragment<BackupRestoreSettingsFragment>(R.string.pref_backup_restore_settings)
        bindPrefFragment<DebugSettingsFragment>(R.string.pref_debug_settings)
        setOnPrefClickListener(R.string.pref_about) {
            requireActivity().startActivity<AboutActivity>()
        }
        setOnPrefClickListener(R.string.pref_feedback) {
            requireActivity().startActivity<FeedbackActivity>()
        }
        setOnPrefClickListener(R.string.pref_check_upgrade) {
            UpgradeUtils.checkUpgrade(this, true,
                onFailed = { requireRootLayout()?.showSnackBar(R.string.update_check_failed) },
                onNoUpgrade = { requireRootLayout()?.showSnackBar(R.string.no_new_update) },
                onFoundUpgrade = { UpgradeDialog.showDialog(childFragmentManager, it) }
            )
        }
    }
}