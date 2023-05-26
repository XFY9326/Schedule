package tool.xfy9326.schedule.ui.activity

import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityAboutBinding
import tool.xfy9326.schedule.ui.activity.base.ViewBindingActivity

class AboutActivity : ViewBindingActivity<ActivityAboutBinding>() {
    override fun onCreateViewBinding() = ActivityAboutBinding.inflate(layoutInflater)

    override fun onBindView(viewBinding: ActivityAboutBinding) {
        super.onBindView(viewBinding)
        setSupportActionBar(viewBinding.toolBarAbout.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onInitView(viewBinding: ActivityAboutBinding) {
        viewBinding.textViewAppVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        viewBinding.layoutEULA.setOnSingleClickListener {
            RawTextActivity.launch(this, R.string.eula_license, R.raw.eula)
        }
        viewBinding.layoutOpenSourceLicense.setOnSingleClickListener {
            RawTextActivity.launch(this, R.string.open_source_license, R.raw.license)
        }
    }
}