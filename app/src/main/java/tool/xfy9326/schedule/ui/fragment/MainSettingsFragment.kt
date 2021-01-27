package tool.xfy9326.schedule.ui.fragment

import android.content.Context
import android.os.Bundle
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.observeEvent
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.ui.activity.AboutActivity
import tool.xfy9326.schedule.ui.activity.FeedbackActivity
import tool.xfy9326.schedule.ui.dialog.UpgradeDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

class MainSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private const val PREFERENCE_ABOUT = "about"
        private const val PREFERENCE_FEEDBACK = "feedback"
        private const val PREFERENCE_CHECK_UPGRADE = "checkUpgrade"
    }

    override val preferenceResId: Int = R.xml.settings_main
    override val titleName: Int = R.string.settings

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(PREFERENCE_ABOUT) {
            requireActivity().startActivity<AboutActivity>()
        }
        setOnPrefClickListener(PREFERENCE_FEEDBACK) {
            requireActivity().startActivity<FeedbackActivity>()
        }
        setOnPrefClickListener(PREFERENCE_CHECK_UPGRADE) {
            requireSettingsViewModel()?.checkUpgrade()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireSettingsViewModel()?.updateInfo?.observeEvent(this) {
            if (it.first) {
                val info = it.second
                if (info == null) {
                    requireRootLayout()?.showShortSnackBar(R.string.no_new_update)
                } else {
                    UpgradeDialog.showDialog(childFragmentManager, info)
                }
            } else {
                requireRootLayout()?.showShortSnackBar(R.string.update_check_failed)
            }
        }
    }
}