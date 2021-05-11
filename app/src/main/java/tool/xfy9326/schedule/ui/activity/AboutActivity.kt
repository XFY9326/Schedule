package tool.xfy9326.schedule.ui.activity

import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityAboutBinding
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.vm.AboutViewModel
import tool.xfy9326.schedule.utils.view.DialogUtils

class AboutActivity : ViewModelActivity<AboutViewModel, ActivityAboutBinding>() {
    override val vmClass = AboutViewModel::class

    override fun onCreateViewBinding() = ActivityAboutBinding.inflate(layoutInflater)

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
        viewBinding.layoutEULA.setOnSingleClickListener {
            viewModel.showEULA()
        }
        viewBinding.layoutOpenSourceLicense.setOnSingleClickListener {
            viewModel.showOpenSourceLicense()
        }
    }
}