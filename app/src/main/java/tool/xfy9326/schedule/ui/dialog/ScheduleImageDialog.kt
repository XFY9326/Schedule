package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.databinding.DialogScheduleImageBinding
import tool.xfy9326.schedule.tools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.utils.setSystemBarAppearance

class ScheduleImageDialog : AppCompatDialogFragment() {
    companion object {
        private val DIALOG_TAG = ScheduleImageDialog::class.java.simpleName
        private const val EXTRA_WEEK_NUM = "EXTRA_WEEK_NUM"
        private const val EXTRA_SAVE_IMAGE = "EXTRA_SAVE_IMAGE"

        fun showDialog(fragmentManager: FragmentManager, weekNum: Int) {
            if (fragmentManager.findFragmentByTag(DIALOG_TAG) != null) return

            ScheduleImageDialog().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_WEEK_NUM, weekNum)
                }
            }.show(fragmentManager, DIALOG_TAG)
        }

        fun setScheduleImageListener(lifecycleOwner: LifecycleOwner, fragmentManager: FragmentManager, listener: (Boolean, Int) -> Unit) {
            fragmentManager.setFragmentResultListener(DIALOG_TAG, lifecycleOwner) { _, result ->
                listener.invoke(result.getBoolean(EXTRA_SAVE_IMAGE), result.getInt(EXTRA_WEEK_NUM))
            }
        }
    }

    override fun getTheme() = R.style.AppTheme_OperationButtonDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        DialogScheduleImageBinding.inflate(layoutInflater, container, false).apply {
            cardViewScheduleImageSave.setOnSingleClickListener {
                reportResult(true)
                dismiss()
            }
            cardViewScheduleImageShare.setOnSingleClickListener {
                reportResult(false)
                dismiss()
            }
            root.setOnSingleClickListener {
                dismiss()
            }
        }.root

    override fun onStart() {
        super.onStart()
        requireDialog().window?.apply {
            lifecycleScope.launch {
                setSystemBarAppearance(ScheduleDataStore.scheduleSystemBarAppearanceFlow.first())
            }
        }
    }

    private fun reportResult(saveImage: Boolean) {
        setFragmentResult(
            requestKey = DIALOG_TAG,
            result = Bundle().apply {
                putInt(EXTRA_WEEK_NUM, requireArguments().getInt(EXTRA_WEEK_NUM))
                putBoolean(EXTRA_SAVE_IMAGE, saveImage)
            }
        )
    }
}