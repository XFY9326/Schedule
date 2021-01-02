package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.transition.Fade
import com.google.android.material.button.MaterialButton
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.kt.getDrawableCompat

class FullScreenLoadingDialog : DialogFragment() {
    companion object {
        private val FRAGMENT_TAG = FullScreenLoadingDialog::class.simpleName
        private const val EXTRA_SHOW_CANCEL_BUTTON = "SHOW_CANCEL_BUTTON"

        fun showDialog(fragmentManager: FragmentManager, showCancel: Boolean = true) {
            FullScreenLoadingDialog().apply {
                arguments = buildBundle {
                    putBoolean(EXTRA_SHOW_CANCEL_BUTTON, showCancel)
                }
            }.show(fragmentManager, FRAGMENT_TAG)
        }

        fun closeDialog(fragmentManager: FragmentManager) {
            (fragmentManager.findFragmentByTag(FRAGMENT_TAG) as? DialogFragment?)?.dismissAllowingStateLoss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Fade()
        exitTransition = Fade()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }

        return LinearLayoutCompat(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            clipChildren = false
            clipToPadding = false
            orientation = LinearLayoutCompat.VERTICAL
            gravity = Gravity.CENTER

            addView(ProgressBar(requireContext()).apply {
                val size = resources.getDimensionPixelOffset(R.dimen.full_screen_loading_dialog_loading_size)
                layoutParams = LinearLayoutCompat.LayoutParams(size, size)
                isIndeterminate = true
            })
            if (requireArguments().getBoolean(EXTRA_SHOW_CANCEL_BUTTON)) {
                addView(MaterialButton(requireContext()).apply {
                    val marginTop = resources.getDimensionPixelOffset(R.dimen.full_screen_loading_dialog_button_margin_top)
                    layoutParams = LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, marginTop, 0, 0)
                    }
                    setText(android.R.string.cancel)
                    setOnClickListener {
                        val owner = requireContext()
                        if (owner is OnRequestCancelListener) {
                            if (owner.onFullScreenLoadingDialogRequestCancel()) {
                                dismiss()
                            }
                        }
                    }
                })
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window?.apply {
                setDimAmount(0.8f)
                setBackgroundDrawable(requireContext().getDrawableCompat(android.R.color.transparent))
            }
        }
    }

    interface OnRequestCancelListener {
        fun onFullScreenLoadingDialogRequestCancel(): Boolean
    }
}