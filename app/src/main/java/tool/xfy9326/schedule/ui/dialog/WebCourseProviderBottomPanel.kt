package tool.xfy9326.schedule.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogWebCourseProviderBottomPanelBinding
import tool.xfy9326.schedule.kt.requireOwner

class WebCourseProviderBottomPanel : BottomSheetDialogFragment() {
    companion object {
        private val TAG_DIALOG = WebCourseProviderBottomPanel::class.simpleName
        const val EXTRA_AUTHOR_NAME = "EXTRA_AUTHOR_NAME"

        fun showDialog(fragmentManager: FragmentManager, authorName: String) {
            WebCourseProviderBottomPanel().apply {
                arguments = bundleOf(
                    EXTRA_AUTHOR_NAME to authorName
                )
            }.show(fragmentManager, TAG_DIALOG)
        }

        fun isShowing(fragmentManager: FragmentManager) = fragmentManager.findFragmentByTag(TAG_DIALOG) != null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val authorName = requireArguments().getString(EXTRA_AUTHOR_NAME)
        val binding = DialogWebCourseProviderBottomPanelBinding.inflate(layoutInflater)

        return BottomSheetDialog(requireContext(), R.style.AppTheme_TransparentBottomSheetDialog).apply {
            setContentView(binding.root)

            binding.textViewCourseAdapterAuthor.text = getString(R.string.adapter_author, authorName)
            binding.buttonImportCourseToNewSchedule.setOnClickListener {
                requireOwner<BottomPanelActionListener>()?.onImportCourseToSchedule(false)
                dismiss()
            }
            binding.buttonImportCourseToCurrentSchedule.setOnClickListener {
                requireOwner<BottomPanelActionListener>()?.onImportCourseToSchedule(true)
                dismiss()
            }

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onStart() {
        super.onStart()
        requireDialog().window?.setDimAmount(0f)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireOwner<BottomPanelActionListener>()?.onBottomPanelDismiss()
    }

    interface BottomPanelActionListener {
        fun onBottomPanelDismiss()

        fun onImportCourseToSchedule(isCurrentSchedule: Boolean)
    }
}