package tool.xfy9326.schedule.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xfy9326.atools.core.startActivity
import io.github.xfy9326.atools.ui.getText
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import io.github.xfy9326.atools.ui.showToast
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogJsConfigImportBinding
import tool.xfy9326.schedule.ui.activity.WebActivity
import tool.xfy9326.schedule.utils.AppUrl

class JSConfigImportDialog : AppCompatDialogFragment() {
    companion object {
        private val DIALOG_TAG = JSConfigImportDialog::class.java.simpleName

        private const val EXTRA_URL = "EXTRA_URL"

        fun showDialog(fragmentManager: FragmentManager, url: String? = null) {
            (fragmentManager.findFragmentByTag(DIALOG_TAG) as? DialogFragment)?.dismissAllowingStateLoss()
            JSConfigImportDialog().apply {
                if (url != null) {
                    arguments = bundleOf(
                        EXTRA_URL to url
                    )
                }
            }.show(fragmentManager, DIALOG_TAG)
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
        val binding = DialogJsConfigImportBinding.inflate(layoutInflater)
        arguments?.getString(EXTRA_URL)?.let {
            binding.editTextJsConfigUrl.setText(it)
            arguments?.remove(EXTRA_URL)
        }
        binding.buttonOnlineJsConfigList.setOnSingleClickListener {
            requireContext().startActivity<WebActivity> {
                putExtra(WebActivity.EXTRA_UA, AppUrl.APP_UA)
                putExtra(WebActivity.EXTRA_NO_CACHE, true)
                putExtra(WebActivity.EXTRA_TITLE, getString(R.string.add_course_import))
                putExtra(WebActivity.EXTRA_URL, AppUrl.JS_COURSE_IMPORT_URL)
            }
        }

        return MaterialAlertDialogBuilder(requireContext()).apply {
            setPositiveButton(R.string.add, null)
            setNegativeButton(android.R.string.cancel, null)
            setNeutralButton(R.string.from_file) { _, _ ->
                setFragmentResult(DIALOG_TAG, Bundle.EMPTY)
            }
            setView(binding.root)
        }.create().also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val inputUrl = binding.editTextJsConfigUrl.text.getText()?.trim()
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