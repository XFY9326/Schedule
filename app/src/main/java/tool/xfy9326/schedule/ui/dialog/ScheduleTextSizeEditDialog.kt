package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.ScheduleText
import tool.xfy9326.schedule.databinding.DialogScheduleTextSizeEditBinding
import tool.xfy9326.schedule.tools.core.getParcelableCompat
import tool.xfy9326.schedule.tools.core.getSerializableCompat
import tool.xfy9326.schedule.tools.ui.setOnSingleClickListener

class ScheduleTextSizeEditDialog : BottomSheetDialogFragment() {
    companion object {
        private val TAG_DIALOG = ScheduleTextSizeEditDialog::class.java.simpleName
        private const val EXTRA_TEXT_TYPE = "EXTRA_TEXT_TYPE"
        private const val EXTRA_TEXT_SIZE = "EXTRA_TEXT_SIZE"
        private const val EXTRA_TEXT_EDIT_TITLE = "EXTRA_TEXT_EDIT_TITLE"
        private const val EXTRA_TEXT_SIZE_VALUE = "EXTRA_TEXT_SIZE_VALUE"

        fun showDialog(fragmentManager: FragmentManager, title: String, scheduleText: ScheduleText, textSize: ScheduleText.TextSize) {
            ScheduleTextSizeEditDialog().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_TEXT_EDIT_TITLE, title)
                    putSerializable(EXTRA_TEXT_TYPE, scheduleText)
                    putParcelable(EXTRA_TEXT_SIZE, textSize)
                }
            }.show(fragmentManager, TAG_DIALOG)
        }

        fun setTextSizeEditListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, listener: (ScheduleText, Int) -> Unit) {
            fragmentManager.setFragmentResultListener(TAG_DIALOG, lifecycleOwner) { _, result ->
                listener.invoke(
                    result.getSerializableCompat(EXTRA_TEXT_TYPE)!!,
                    result.getInt(EXTRA_TEXT_SIZE_VALUE)
                )
            }
        }
    }

    private val textSize by lazy { requireArguments().getParcelableCompat<ScheduleText.TextSize>(EXTRA_TEXT_SIZE)!! }
    private val textType by lazy { requireArguments().getSerializableCompat<ScheduleText>(EXTRA_TEXT_TYPE)!! }
    private val binding by lazy { DialogScheduleTextSizeEditBinding.inflate(layoutInflater) }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        BottomSheetDialog(requireContext(), R.style.AppTheme_TransparentBottomSheetDialog).apply {
            binding.apply {
                val currentTextSize =
                    (savedInstanceState?.getInt(EXTRA_TEXT_SIZE_VALUE, textSize.getRaw(textType)) ?: textSize.getRaw(textType)).toFloat()
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
        setFragmentResult(
            requestKey = TAG_DIALOG,
            result = Bundle().apply {
                putSerializable(EXTRA_TEXT_TYPE, textType)
                putInt(EXTRA_TEXT_SIZE_VALUE, value)
            }
        )
    }
}