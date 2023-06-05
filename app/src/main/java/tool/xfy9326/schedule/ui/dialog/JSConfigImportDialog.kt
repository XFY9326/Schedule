package tool.xfy9326.schedule.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import io.github.xfy9326.atools.ui.getText
import io.github.xfy9326.atools.ui.showToast
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogEditTextBinding

class JSConfigImportDialog : AppCompatDialogFragment() {
    companion object {
        private val DIALOG_TAG = JSConfigImportDialog::class.java.simpleName

        private const val EXTRA_URL = "EXTRA_URL"

        fun showDialog(fragmentManager: FragmentManager) {
            JSConfigImportDialog().show(fragmentManager, DIALOG_TAG)
        }

        fun setOnJSConfigImportListener(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            onUrlImport: (String) -> Unit,
            onFileImport: () -> Unit
        ) {
            fragmentManager.setFragmentResultListener(DIALOG_TAG, lifecycleOwner) { _, bundle ->
                if (bundle.containsKey(EXTRA_URL)) {
                    onUrlImport(bundle.getString(EXTRA_URL)!!)
                } else {
                    onFileImport()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogEditTextBinding.inflate(layoutInflater)

        return MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.add_course_import)
            binding.textLayoutDialogText.setHint(R.string.course_import_config_url_hint)
            binding.textLayoutDialogText.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            binding.editTextDialogText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

            setPositiveButton(R.string.add, null)
            setNegativeButton(android.R.string.cancel, null)
            setNeutralButton(R.string.from_file) { _, _ ->
                setFragmentResult(DIALOG_TAG, Bundle.EMPTY)
            }
            setView(binding.root)
        }.create().also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val inputUrl = binding.editTextDialogText.text.getText()?.trim()
                    if (inputUrl != null && URLUtil.isValidUrl(inputUrl)) {
                        setFragmentResult(DIALOG_TAG, bundleOf(EXTRA_URL to inputUrl))
                        dismiss()
                    } else {
                        requireContext().showToast(R.string.url_invalid)
                    }
                }
            }
        }
    }
}