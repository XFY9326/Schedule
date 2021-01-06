package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.kt.getDrawableCompat

class FullScreenLoadingDialog : DialogFragment() {
    companion object {
        private val FRAGMENT_TAG = FullScreenLoadingDialog::class.simpleName
        private const val EXTRA_SHOW_CANCEL_BUTTON = "SHOW_CANCEL_BUTTON"

        fun createControllerInstance(activity: AppCompatActivity) =
            Controller(activity.lifecycleScope, activity.supportFragmentManager)

        fun createControllerInstance(fragment: Fragment) =
            Controller(fragment.lifecycleScope, fragment.childFragmentManager)
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

    class Controller constructor(
        private val lifeCycleScope: CoroutineScope,
        private val fragmentManager: FragmentManager,
    ) {
        companion object {
            private const val MIN_SHOW_TIME_MS = 500L
            private const val MIN_DELAY_MS = 200L
        }

        private var showJob: Job? = null
        private var hideJob: Job? = null

        private var mStartTime = -1L
        private var mPostedHide = false
        private var mPostedShow = false
        private var mDismissed = false

        init {
            fragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                        if (f.tag == FRAGMENT_TAG) removeCallbacks()
                    }

                    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                        if (f.tag == FRAGMENT_TAG) removeCallbacks()
                    }
                }, false
            )
        }

        private fun removeCallbacks() {
            showJob?.cancel()
            showJob = null
            hideJob?.cancel()
            hideJob = null
        }

        private fun delayedHide() {
            mPostedHide = false
            mStartTime = -1
            closeDialog(fragmentManager)
        }

        private fun delayedShow(showCancel: Boolean) {
            mPostedShow = false
            if (!mDismissed) {
                mStartTime = System.currentTimeMillis()
                showDialog(fragmentManager, showCancel)
            }
        }

        private fun showDialog(fragmentManager: FragmentManager, showCancel: Boolean) {
            FullScreenLoadingDialog().apply {
                arguments = buildBundle {
                    putBoolean(EXTRA_SHOW_CANCEL_BUTTON, showCancel)
                }
            }.show(fragmentManager, FRAGMENT_TAG)
        }

        private fun closeDialog(fragmentManager: FragmentManager) {
            (fragmentManager.findFragmentByTag(FRAGMENT_TAG) as? DialogFragment?)?.dismissAllowingStateLoss()
        }

        @Synchronized
        fun show(showCancel: Boolean = true) {
            mStartTime = -1
            mDismissed = false
            hideJob?.cancel()
            hideJob = null
            mPostedHide = false
            if (!mPostedShow) {
                showJob = lifeCycleScope.launch(Dispatchers.Default) {
                    delay(MIN_DELAY_MS)
                    launch(Dispatchers.Main) {
                        delayedShow(showCancel)
                        showJob = null
                    }
                }
                mPostedShow = true
            }
        }

        @Synchronized
        fun hide() {
            mDismissed = true
            showJob?.cancel()
            showJob = null
            mPostedShow = false
            val diff = System.currentTimeMillis() - mStartTime
            if (diff >= MIN_SHOW_TIME_MS || mStartTime == -1L) {
                closeDialog(fragmentManager)
            } else {
                if (!mPostedHide) {
                    hideJob = lifeCycleScope.launch(Dispatchers.Default) {
                        delay(MIN_SHOW_TIME_MS - diff)
                        launch(Dispatchers.Main) {
                            delayedHide()
                            hideJob = null
                        }
                    }
                    mPostedHide = true
                }
            }
        }
    }
}