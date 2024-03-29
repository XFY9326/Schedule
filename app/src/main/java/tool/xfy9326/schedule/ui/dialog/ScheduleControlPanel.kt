package tool.xfy9326.schedule.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.databinding.DialogScheduleControlPanelBinding
import tool.xfy9326.schedule.utils.consumeBottomInsets
import tool.xfy9326.schedule.utils.setSystemBarAppearance

class ScheduleControlPanel : BottomSheetDialogFragment() {
    companion object {
        private val DIALOG_TAG = ScheduleControlPanel::class.java.simpleName

        private const val EXTRA_SCROLL_TO_WEEK = "SCROLL_TO_WEEK"
        private const val ARGUMENT_CURRENT_SHOW_WEEK_NUM = "CURRENT_SHOW_WEEK_NUM"
        private const val ARGUMENT_NOW_WEEK_NUM = "NOW_WEEK_NUM"
        private const val ARGUMENT_MAX_WEEK_NUM = "MAX_WEEK_NUM"

        fun showDialog(fragmentManager: FragmentManager, currentShowWeekNum: Int, nowWeekNum: Int, maxWeekNum: Int) {
            if (fragmentManager.findFragmentByTag(DIALOG_TAG) != null) return

            ScheduleControlPanel().apply {
                arguments = bundleOf(
                    ARGUMENT_CURRENT_SHOW_WEEK_NUM to currentShowWeekNum,
                    ARGUMENT_NOW_WEEK_NUM to nowWeekNum,
                    ARGUMENT_MAX_WEEK_NUM to maxWeekNum,
                )
            }.show(fragmentManager, DIALOG_TAG)
        }

        fun addScrollToWeekListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, block: (Int) -> Unit) {
            fragmentManager.setFragmentResultListener(DIALOG_TAG, lifecycleOwner) { _, bundle ->
                block(bundle.getInt(EXTRA_SCROLL_TO_WEEK))
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentShowWeekNum = requireArguments().getInt(ARGUMENT_CURRENT_SHOW_WEEK_NUM)
        val nowWeekNum = requireArguments().getInt(ARGUMENT_NOW_WEEK_NUM)
        val maxWeekNum = requireArguments().getInt(ARGUMENT_MAX_WEEK_NUM)
        val binding = DialogScheduleControlPanelBinding.inflate(layoutInflater)

        return BottomSheetDialog(requireContext(), R.style.AppTheme_TransparentBottomSheetDialog).apply {
            setContentView(binding.root)

            if (nowWeekNum != 0) binding.textViewScheduleControlCurrentWeekInfo.text = getString(R.string.current_week_title, nowWeekNum)

            binding.sliderScheduleControlWeekNum.apply {
                valueTo = maxWeekNum.toFloat()
                value = currentShowWeekNum.toFloat()

                setLabelFormatter {
                    context.getString(R.string.week_num, it.toInt())
                }

                setOnSlideValueSetListener(this@ScheduleControlPanel::changeShowWeekNum)
            }

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onStart() {
        super.onStart()
        requireDialog().window?.apply {
            lifecycleScope.launch {
                setSystemBarAppearance(ScheduleDataStore.scheduleSystemBarAppearanceFlow.first())
            }
        }
        consumeBottomInsets()
    }

    private fun changeShowWeekNum(value: Float) {
        setFragmentResult(
            DIALOG_TAG, bundleOf(
                EXTRA_SCROLL_TO_WEEK to value.toInt()
            )
        )
    }
}