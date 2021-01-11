package tool.xfy9326.schedule.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogWebCourseProviderBottomPanelBinding
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.kt.requireOwner

class WebCourseProviderBottomPanel : BottomSheetDialogFragment() {
    companion object {
        private val TAG_DIALOG = WebCourseProviderBottomPanel::class.simpleName
        const val EXTRA_AUTHOR_NAME = "EXTRA_AUTHOR_NAME"

        fun showDialog(fragmentManager: FragmentManager, authorName: String) {
            WebCourseProviderBottomPanel().apply {
                arguments = buildBundle {
                    putString(EXTRA_AUTHOR_NAME, authorName)
                }
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
                requireOwner<OnWebCourseProviderBottomPanelOperateListener>()?.onImportCourseToNewSchedule()
                dismiss()
            }
            binding.buttonImportCourseToCurrentSchedule.setOnClickListener {
                requireOwner<OnWebCourseProviderBottomPanelOperateListener>()?.onImportCourseToCurrentSchedule()
                dismiss()
            }

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setDimAmount(0f)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireOwner<OnWebCourseProviderBottomPanelOperateListener>()?.onWebCourseProviderBottomPanelDismiss()
    }

    interface OnWebCourseProviderBottomPanelOperateListener {
        fun onWebCourseProviderBottomPanelDismiss()

        fun onImportCourseToCurrentSchedule()

        fun onImportCourseToNewSchedule()
    }
}