package tool.xfy9326.schedule.ui.activity

import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityAboutBinding
import tool.xfy9326.schedule.kt.observeEvent
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.vm.AboutViewModel
import tool.xfy9326.schedule.utils.DialogUtils

class AboutActivity : ViewModelActivity<AboutViewModel, ActivityAboutBinding>() {
    override fun onPrepare(viewBinding: ActivityAboutBinding, viewModel: AboutViewModel) {
        setSupportActionBar(viewBinding.toolBarAbout.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBindLiveData(viewBinding: ActivityAboutBinding, viewModel: AboutViewModel) {
        viewModel.showEULA.observeEvent(this) {
            DialogUtils.showEULADialog(this, it)
        }
        viewModel.showOpenSourceLicense.observeEvent(this) {
            DialogUtils.showOpenSourceLicenseDialog(this, it)
        }
    }

    override fun onInitView(viewBinding: ActivityAboutBinding, viewModel: AboutViewModel) {
        viewBinding.textViewAppVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        viewBinding.layoutEULA.setOnClickListener {
            viewModel.showEULA()
        }
        viewBinding.layoutOpenSourceLicense.setOnClickListener {
            viewModel.showOpenSourceLicense()
        }
        viewBinding.layoutFeedbackOnline.setOnClickListener {
            startActivity<FeedbackActivity>()
        }
    }
}