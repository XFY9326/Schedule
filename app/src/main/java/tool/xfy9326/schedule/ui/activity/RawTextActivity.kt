package tool.xfy9326.schedule.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import io.github.xfy9326.atools.core.startActivity
import io.github.xfy9326.atools.livedata.observeEvent
import tool.xfy9326.schedule.databinding.ActivityRawTextBinding
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.vm.RawTextViewModel

class RawTextActivity : ViewModelActivity<RawTextViewModel, ActivityRawTextBinding>() {
    companion object {
        private const val INTENT_EXTRA_TITLE_RES_ID = "TITLE_RES_ID"
        private const val INTENT_EXTRA_CONTENT_RES_ID = "CONTENT_RES_ID"

        fun launch(context: Context, @StringRes titleResId: Int, @RawRes contentResId: Int) {
            context.startActivity<RawTextActivity> {
                putExtra(INTENT_EXTRA_TITLE_RES_ID, titleResId)
                putExtra(INTENT_EXTRA_CONTENT_RES_ID, contentResId)
            }
        }
    }

    override val vmClass = RawTextViewModel::class

    override fun onCreateViewBinding() = ActivityRawTextBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityRawTextBinding, viewModel: RawTextViewModel) {
        setSupportActionBar(viewBinding.toolBarRawText.toolBarGeneral)
        supportActionBar?.setTitle(intent.getIntExtra(INTENT_EXTRA_TITLE_RES_ID, 0))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onContentViewPreload(savedInstanceState: Bundle?, viewModel: RawTextViewModel) {
        if (isFirstLaunch) {
            viewModel.loadRawText(intent.getIntExtra(INTENT_EXTRA_CONTENT_RES_ID, 0))
        }
    }

    override fun onBindLiveData(viewBinding: ActivityRawTextBinding, viewModel: RawTextViewModel) {
        viewModel.rawText.observeEvent(this) {
            viewBinding.textViewContent.text = it
            viewBinding.progressBarRawTextLoading.hide()
            viewBinding.textViewContent.visibility = View.VISIBLE
        }
    }
}