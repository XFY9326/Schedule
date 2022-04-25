package tool.xfy9326.schedule.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogWebCourseProviderBottomPanelBinding

class WebCourseProviderBottomPanel : BottomSheetDialogFragment() {
    companion object {
        private val DIALOG_TAG = WebCourseProviderBottomPanel::class.java.simpleName

        private const val EXTRA_AUTHOR_NAME = "EXTRA_AUTHOR_NAME"
        private const val EXTRA_IS_CURRENT_SCHEDULE = "EXTRA_IS_CURRENT_SCHEDULE"

        fun showDialog(fragmentManager: FragmentManager, authorName: String) {
            WebCourseProviderBottomPanel().apply {
                arguments = bundleOf(
                    EXTRA_AUTHOR_NAME to authorName
                )
            }.show(fragmentManager, DIALOG_TAG)
        }

        fun isShowing(fragmentManager: FragmentManager) = fragmentManager.findFragmentByTag(DIALOG_TAG) != null

        fun setBottomPanelActionListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, onDismiss: () -> Unit, onImport: (Boolean) -> Unit) {
            fragmentManager.setFragmentResultListener(DIALOG_TAG, lifecycleOwner) { _, bundle ->
                if (bundle.containsKey(EXTRA_IS_CURRENT_SCHEDULE)) {
                    onImport(bundle.getBoolean(EXTRA_IS_CURRENT_SCHEDULE))
                } else {
                    onDismiss()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val authorName = requireArguments().getString(EXTRA_AUTHOR_NAME)
        val binding = DialogWebCourseProviderBottomPanelBinding.inflate(layoutInflater)

        return BottomSheetDialog(requireContext(), R.style.AppTheme_TransparentBottomSheetDialog).apply {
            setContentView(binding.root)

            binding.textViewCourseAdapterAuthor.text = getString(R.string.adapter_author, authorName)
            binding.buttonImportCourseToNewSchedule.setOnSingleClickListener {
                reportResult(false)
                dismiss()
            }
            binding.buttonImportCourseToCurrentSchedule.setOnSingleClickListener {
                reportResult(true)
                dismiss()
            }

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        reportResult()
    }

    private fun reportResult(isCurrentSchedule: Boolean? = null) {
        setFragmentResult(DIALOG_TAG, if (isCurrentSchedule == null) Bundle.EMPTY else bundleOf(EXTRA_IS_CURRENT_SCHEDULE to isCurrentSchedule))
    }
}