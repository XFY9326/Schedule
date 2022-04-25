package tool.xfy9326.schedule.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xfy9326.atools.ui.getText
import io.github.xfy9326.atools.ui.showToast
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogEditTextBinding
import kotlin.properties.Delegates

class NumberEditDialog : AppCompatDialogFragment() {
    companion object {
        private val DIALOG_TAG = NumberEditDialog::class.java.simpleName

        private const val EXTRA_NUMBER = "EXTRA_NUMBER"
        private const val EXTRA_MIN_NUMBER = "EXTRA_MIN_NUMBER"
        private const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val EXTRA_EDIT_HINT = "EXTRA_EDIT_HINT"
        private const val EXTRA_TAG = "EXTRA_TAG"
        private const val EXTRA_MAX_NUMBER = "EXTRA_MAX_NUMBER"
        private const val MAX_INPUT_AMOUNT = Int.MAX_VALUE.toString().length

        fun showDialog(
            fragmentManager: FragmentManager,
            tag: String?,
            number: Int,
            minNumber: Int,
            maxNumber: Int,
            title: String,
            editHint: String,
        ) {
            NumberEditDialog().apply {
                arguments = bundleOf(
                    EXTRA_TAG to tag,
                    EXTRA_NUMBER to number,
                    EXTRA_MIN_NUMBER to minNumber,
                    EXTRA_MAX_NUMBER to maxNumber,
                    EXTRA_TITLE to title,
                    EXTRA_EDIT_HINT to editHint
                )
            }.show(fragmentManager, tag)
        }

        fun setOnNumberChangedListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, block: (tag: String?, number: Int) -> Unit) {
            fragmentManager.setFragmentResultListener(DIALOG_TAG, lifecycleOwner) { _, bundle ->
                block(
                    bundle.getString(EXTRA_TAG),
                    bundle.getInt(EXTRA_NUMBER)
                )
            }
        }
    }

    private val minNumber by lazy { requireArguments().getInt(EXTRA_MIN_NUMBER) }
    private val maxNumber by lazy { requireArguments().getInt(EXTRA_MAX_NUMBER) }

    private var number by Delegates.notNull<Int>()
    private val binding by lazy { DialogEditTextBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        number = savedInstanceState?.getInt(EXTRA_NUMBER) ?: requireArguments().getInt(EXTRA_NUMBER)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(EXTRA_NUMBER, binding.editTextDialogText.text.getText()?.toIntOrNull() ?: requireArguments().getInt(EXTRA_NUMBER))
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(requireArguments().getString(EXTRA_TITLE))
            binding.textLayoutDialogText.hint = requireArguments().getString(EXTRA_EDIT_HINT)
            binding.textLayoutDialogText.isErrorEnabled = true
            binding.textLayoutDialogText.isCounterEnabled = true
            binding.textLayoutDialogText.counterMaxLength = MAX_INPUT_AMOUNT
            binding.root.updatePadding(bottom = 0)

            binding.editTextDialogText.inputType = InputType.TYPE_CLASS_NUMBER
            binding.editTextDialogText.filters = arrayOf(InputFilter.LengthFilter(MAX_INPUT_AMOUNT))
            binding.editTextDialogText.doAfterTextChanged {
                binding.textLayoutDialogText.error = getInputErrorMsg(it.getText())
            }
            binding.editTextDialogText.setText(number.toString())

            setPositiveButton(android.R.string.ok, null)
            setNegativeButton(android.R.string.cancel, null)
            setView(binding.root)
        }.create().also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val input = binding.editTextDialogText.text.getText()
                    val msg = getInputErrorMsg(input)
                    if (msg == null) {
                        val number = input!!.toInt()
                        if (number != requireArguments().getInt(EXTRA_NUMBER)) {
                            setFragmentResult(
                                DIALOG_TAG,
                                bundleOf(
                                    EXTRA_TAG to requireArguments().getString(EXTRA_TAG),
                                    EXTRA_NUMBER to number
                                )
                            )
                        }
                        dismiss()
                    } else {
                        requireContext().showToast(msg)
                    }
                }
            }
        }

    private fun getInputErrorMsg(input: String?): String? =
        when {
            input == null -> getString(R.string.input_number_error)
            input.length > MAX_INPUT_AMOUNT -> getString(R.string.input_value_too_large)
            else -> {
                val number = input.toLong()
                when {
                    number > maxNumber || number > Int.MAX_VALUE -> getString(R.string.input_value_too_large)
                    number < minNumber || number < Int.MIN_VALUE -> getString(R.string.input_value_too_small)
                    else -> null
                }
            }
        }
}