package tool.xfy9326.schedule.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogScheduleControlPanelBinding
import tool.xfy9326.schedule.kt.enableLightSystemBar
import tool.xfy9326.schedule.kt.isUsingNightMode
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel

class ScheduleControlPanel : BottomSheetDialogFragment() {
    companion object {
        private const val ARGUMENT_CURRENT_SHOW_WEEK_NUM = "CURRENT_SHOW_WEEK_NUM"
        private const val ARGUMENT_NOW_WEEK_NUM = "NOW_WEEK_NUM"
        private const val ARGUMENT_MAX_WEEK_NUM = "MAX_WEEK_NUM"

        fun showDialog(fragmentManager: FragmentManager, currentShowWeekNum: Int, nowWeekNum: Int, maxWeekNum: Int) {
            ScheduleControlPanel().apply {
                arguments = bundleOf(
                    ARGUMENT_CURRENT_SHOW_WEEK_NUM to currentShowWeekNum,
                    ARGUMENT_NOW_WEEK_NUM to nowWeekNum,
                    ARGUMENT_MAX_WEEK_NUM to maxWeekNum
                )
            }.show(fragmentManager, null)
        }
    }

    private lateinit var viewModel: ScheduleViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity())[ScheduleViewModel::class.java]
        viewModel.useLightColorSystemBarColor.observe(this) {
            lifecycleScope.launchWhenStarted {
                // Light status bar in Android Window means status bar that used in light background, so the status bar color is black.
                // For default, it's true in app theme.
                dialog?.window?.enableLightSystemBar(context, !it && !requireContext().isUsingNightMode())
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

    private fun changeShowWeekNum(value: Float) {
        viewModel.scrollToWeek.postEvent(value.toInt())
    }
}