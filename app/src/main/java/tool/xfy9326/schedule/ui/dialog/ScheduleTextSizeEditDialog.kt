package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import lib.xfy9326.android.kit.setOnSingleClickListener
import lib.xfy9326.kit.castNonNull
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.ScheduleText
import tool.xfy9326.schedule.databinding.DialogScheduleTextSizeEditBinding

class ScheduleTextSizeEditDialog : BottomSheetDialogFragment() {
    companion object {
        private val TAG_DIALOG = ScheduleTextSizeEditDialog::class.java.simpleName
        private const val EXTRA_TEXT_TYPE = "EXTRA_TEXT_TYPE"
        private const val EXTRA_TEXT_SIZE = "EXTRA_TEXT_SIZE"
        private const val EXTRA_TEXT_EDIT_TITLE = "EXTRA_TEXT_EDIT_TITLE"
        private const val EXTRA_TEXT_SIZE_VALUE = "EXTRA_TEXT_SIZE_VALUE"

        fun showDialog(fragmentManager: FragmentManager, title: String, scheduleText: ScheduleText, textSize: ScheduleText.TextSize) {
            ScheduleTextSizeEditDialog().apply {
                arguments = bundleOf(
                    EXTRA_TEXT_EDIT_TITLE to title,
                    EXTRA_TEXT_TYPE to scheduleText,
                    EXTRA_TEXT_SIZE to textSize
                )
            }.show(fragmentManager, TAG_DIALOG)
        }

        fun setTextSizeEditListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, listener: (ScheduleText, Int) -> Unit) {
            fragmentManager.setFragmentResultListener(TAG_DIALOG, lifecycleOwner) { _, result ->
                listener.invoke(
                    result.getSerializable(EXTRA_TEXT_TYPE).castNonNull(),
                    result.getInt(EXTRA_TEXT_SIZE_VALUE)
                )
            }
        }
    }

    private val textSize by lazy { requireArguments().getParcelable<ScheduleText.TextSize>(EXTRA_TEXT_SIZE)!! }
    private val textType by lazy { requireArguments().getSerializable(EXTRA_TEXT_TYPE).castNonNull<ScheduleText>() }
    private val binding by lazy { DialogScheduleTextSizeEditBinding.inflate(layoutInflater) }

    override fun onCreateDialog(savedInstanceState: Bundle?) = BottomSheetDialog(requireContext(), R.style.AppTheme_TransparentBottomSheetDialog).apply {
        binding.apply {
            val currentTextSize = (savedInstanceState?.getInt(EXTRA_TEXT_SIZE_VALUE, textSize.getRaw(textType)) ?: textSize.getRaw(textType)).toFloat()
            textViewTextSizeEditSample.textSize = currentTextSize + ScheduleText.TextSize.sizeOffset
            textViewTextSizeEditTitle.text = requireArguments().getString(EXTRA_TEXT_EDIT_TITLE)
            sliderTextSizeEdit.apply {
                value = currentTextSize
                valueFrom = ScheduleText.TextSize.minSize.toFloat()
                valueTo = ScheduleText.TextSize.maxSize.toFloat()
                addOnChangeListener { _, value, _ ->
                    textViewTextSizeEditSample.textSize = value + ScheduleText.TextSize.sizeOffset
                }
            }
            buttonTextSizeEditCancel.setOnSingleClickListener {
                dismiss()
            }
            buttonTextSizeEditConfirm.setOnSingleClickListener {
                reportResult(binding.sliderTextSizeEdit.value.toInt())
                dismiss()
            }
        }
        setContentView(binding.root)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_TEXT_SIZE_VALUE, binding.sliderTextSizeEdit.value.toInt())
    }

    private fun reportResult(value: Int) {
        setFragmentResult(TAG_DIALOG, bundleOf(
            EXTRA_TEXT_TYPE to textType,
            EXTRA_TEXT_SIZE_VALUE to value
        ))
    }
}