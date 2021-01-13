package tool.xfy9326.schedule.ui.fragment

import android.os.Bundle
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.ui.activity.AboutActivity
import tool.xfy9326.schedule.ui.activity.FeedbackActivity
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

class MainSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private const val PREFERENCE_ABOUT = "about"
        private const val PREFERENCE_FEEDBACK = "feedback"
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
    }
}