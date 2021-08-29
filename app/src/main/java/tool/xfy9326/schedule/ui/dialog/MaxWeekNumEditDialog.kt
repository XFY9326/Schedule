package tool.xfy9326.schedule.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import lib.xfy9326.android.kit.getText
import lib.xfy9326.android.kit.showToast
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogEditTextBinding
import tool.xfy9326.schedule.utils.schedule.ScheduleUtils
import kotlin.properties.Delegates

class MaxWeekNumEditDialog : AppCompatDialogFragment() {
    companion object {
        private val DIALOG_TAG = MaxWeekNumEditDialog::class.java.simpleName

        private const val EXTRA_WEEK_NUM = "EXTRA_WEEK_NUM"
        private const val MAX_INPUT_AMOUNT = Int.MAX_VALUE.toString().length

        fun showDialog(fragmentManager: FragmentManager, weekNum: Int) {
            MaxWeekNumEditDialog().apply {
                arguments = bundleOf(
                    EXTRA_WEEK_NUM to weekNum
                )
            }.show(fragmentManager, DIALOG_TAG)
        }

        fun setOnWeekNumChangedListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, block: (Int) -> Unit) {
            fragmentManager.setFragmentResultListener(DIALOG_TAG, lifecycleOwner) { _, bundle ->
                block(bundle.getInt(EXTRA_WEEK_NUM))
            }
        }
    }

    private var weekNum by Delegates.notNull<Int>()
    private val binding by lazy { DialogEditTextBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weekNum = savedInstanceState?.getInt(EXTRA_WEEK_NUM) ?: requireArguments().getInt(EXTRA_WEEK_NUM)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(EXTRA_WEEK_NUM, binding.editTextDialogText.text.getText()?.toIntOrNull() ?: requireArguments().getInt(EXTRA_WEEK_NUM))
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.schedule_week_num)
            setView(binding.root)
            binding.textLayoutDialogText.setHint(R.string.week_num_title)
            binding.textLayoutDialogText.isErrorEnabled = true
            binding.root.updatePadding(bottom = 0)

            binding.editTextDialogText.inputType = InputType.TYPE_CLASS_NUMBER
            binding.editTextDialogText.maxLines = 1
            binding.editTextDialogText.doAfterTextChanged {
                binding.textLayoutDialogText.error = getInputErrorMsg(it.getText())
            }
            binding.editTextDialogText.setText(weekNum.toString())

            setPositiveButton(android.R.string.ok, null)
            setNegativeButton(android.R.string.cancel, null)
        }.create().also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val weekNumStr = binding.editTextDialogText.text.getText()
                    val msg = getInputErrorMsg(weekNumStr)
                    if (msg == null) {
                        val weekNum = weekNumStr!!.toInt()
                        if (weekNum != requireArguments().getInt(EXTRA_WEEK_NUM)) {
                            setFragmentResult(DIALOG_TAG, bundleOf(EXTRA_WEEK_NUM to weekNum))
                        }
                        dismiss()
                    } else {
                        showToast(msg)
                    }
                }
            }
        }

    private fun getInputErrorMsg(input: String?): String? =
        when {
            input == null -> getString(R.string.schedule_week_num_error)
            input.length > MAX_INPUT_AMOUNT -> getString(R.string.input_value_too_large)
            else -> {
                val weekNum = input.toIntOrNull() ?: -1
                when {
                    weekNum > ScheduleUtils.MAX_WEEK_NUM || weekNum < 0 -> getString(R.string.input_value_too_large)
                    weekNum == 0 -> getString(R.string.schedule_week_num_error)
                    else -> null
                }
            }
        }
}