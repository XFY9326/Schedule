package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.parcelize.Parcelize
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.annotation.Sp
import tool.xfy9326.schedule.databinding.DialogTextSizeEditBinding
import tool.xfy9326.schedule.kt.setOnSingleClickListener

class TextSizeEditDialog : BottomSheetDialogFragment() {
    companion object {
        private val TAG_DIALOG = TextSizeEditDialog::class.java.simpleName
        private const val EXTRA_TEXT_SIZE_PARAMS = "EXTRA_TEXT_SIZE_PARAMS"
        private const val EXTRA_TEXT_SIZE_TAG = "EXTRA_TEXT_SIZE_TAG"
        private const val EXTRA_TEXT_SIZE_VALUE = "EXTRA_TEXT_SIZE_VALUE"

        fun showDialog(fragmentManager: FragmentManager, tag: String, params: Params) {
            TextSizeEditDialog().apply {
                arguments = bundleOf(
                    EXTRA_TEXT_SIZE_PARAMS to params,
                    EXTRA_TEXT_SIZE_TAG to tag
                )
            }.show(fragmentManager, TAG_DIALOG)
        }

        fun setTextSizeEditListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, listener: (String, Int) -> Unit) {
            fragmentManager.setFragmentResultListener(TAG_DIALOG, lifecycleOwner) { _, result ->
                listener.invoke(
                    result.getString(EXTRA_TEXT_SIZE_TAG, EXTRA_TEXT_SIZE_TAG),
                    result.getInt(EXTRA_TEXT_SIZE_VALUE)
                )
            }
        }
    }

    private val params by lazy {
        requireArguments().getParcelable<Params>(EXTRA_TEXT_SIZE_PARAMS)!!
    }
    private val binding by lazy {
        DialogTextSizeEditBinding.inflate(layoutInflater)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = BottomSheetDialog(requireContext(), R.style.AppTheme_TransparentBottomSheetDialog).apply {
        binding.apply {
            val currentTextSize = (savedInstanceState?.getInt(EXTRA_TEXT_SIZE_VALUE, params.textSize) ?: params.textSize).toFloat()
            textViewTextSizeEditSample.textSize = currentTextSize + params.textSizeOffset
            textViewTextSizeEditTitle.text = params.title
            sliderTextSizeEdit.apply {
                value = currentTextSize
                valueFrom = params.valueFrom.toFloat()
                valueTo = params.valueTo.toFloat()
                addOnChangeListener { _, value, _ ->
                    textViewTextSizeEditSample.textSize = value + params.textSizeOffset
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
            EXTRA_TEXT_SIZE_VALUE to value,
            EXTRA_TEXT_SIZE_TAG to requireArguments().getString(EXTRA_TEXT_SIZE_TAG)
        ))
    }

    @Parcelize
    class Params(@Sp val textSize: Int, val textSizeOffset: Int, val valueFrom: Int, val valueTo: Int, val title: String) : Parcelable
}