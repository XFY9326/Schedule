package tool.xfy9326.schedule.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.webkit.URLUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogEditTextBinding
import tool.xfy9326.schedule.kt.getText
import tool.xfy9326.schedule.kt.requireOwner
import tool.xfy9326.schedule.kt.showToast

class JSConfigImportDialog : DialogFragment() {
    companion object {
        private val DIALOG_TAG = JSConfigImportDialog::class.simpleName

        fun showDialog(fragmentManager: FragmentManager) {
            JSConfigImportDialog().show(fragmentManager, DIALOG_TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogEditTextBinding.inflate(layoutInflater)

        return MaterialAlertDialogBuilder(requireContext()).apply {

            setTitle(R.string.add_course_import)
            setView(binding.root)
            binding.layoutDialogText.setHint(R.string.please_input_course_import_config_url)

            setPositiveButton(R.string.add, null)
            setNegativeButton(android.R.string.cancel, null)
            setNeutralButton(R.string.from_file) { _, _ ->
                requireOwner<OnJSConfigImportListener>()?.onJSConfigFileImport()
            }
        }.create().also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val inputUrl = binding.editTextDialogText.text.getText()?.trim()
                    if (inputUrl != null && URLUtil.isValidUrl(inputUrl)) {
                        requireOwner<OnJSConfigImportListener>()?.onJSConfigUrlImport(inputUrl)
                        dismiss()
                    } else {
                        showToast(R.string.url_invalid)
                    }
                }
            }
        }
    }

    interface OnJSConfigImportListener {
        fun onJSConfigUrlImport(url: String)

        fun onJSConfigFileImport()
    }
}