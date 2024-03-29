package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.os.bundleOf
import androidx.core.view.updateMargins
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import com.google.android.material.button.MaterialButton
import io.github.xfy9326.atools.ui.getDrawableCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import java.lang.ref.WeakReference

class FullScreenLoadingDialog : AppCompatDialogFragment() {
    companion object {
        private val FRAGMENT_TAG = FullScreenLoadingDialog::class.java.simpleName
        private const val EXTRA_SHOW_CANCEL_BUTTON = "SHOW_CANCEL_BUTTON"
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
                indeterminateDrawable = requireContext().getDrawableCompat(R.drawable.background_progress_bar_circle)
            })
            if (requireArguments().getBoolean(EXTRA_SHOW_CANCEL_BUTTON)) {
                addView(MaterialButton(requireContext()).apply {
                    val marginTop = resources.getDimensionPixelOffset(R.dimen.full_screen_loading_dialog_button_margin_top)
                    layoutParams = LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                    ).apply {
                        updateMargins(top = marginTop)
                    }
                    setText(android.R.string.cancel)
                    setOnClickListener {
                        setFragmentResult(FRAGMENT_TAG, Bundle.EMPTY)
                    }
                })
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireDialog().apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window?.apply {
                setDimAmount(0.8f)
                setBackgroundDrawable(requireContext().getDrawableCompat(android.R.color.transparent))
            }
        }
    }

    class Controller private constructor(lifecycleOwner: LifecycleOwner, fragmentManager: FragmentManager) :
        CoroutineScope by lifecycleOwner.lifecycleScope {
        companion object {
            private const val MIN_SHOW_TIME_MS = 500L
            private const val MIN_DELAY_MS = 200L

            fun newInstance(lifecycleOwner: LifecycleOwner, fragmentManager: FragmentManager) = Controller(lifecycleOwner, fragmentManager)
        }

        private val weakLifecycleOwner = WeakReference(lifecycleOwner)
        private val weakFragmentManager = WeakReference(fragmentManager)

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
            weakFragmentManager.get()?.let(::closeDialog)
        }

        private fun delayedShow(showCancel: Boolean) {
            mPostedShow = false
            if (!mDismissed) {
                mStartTime = System.currentTimeMillis()
                weakFragmentManager.get()?.let {
                    showDialog(it, showCancel)
                }
            }
        }

        private fun showDialog(fragmentManager: FragmentManager, showCancel: Boolean) {
            if (fragmentManager.findFragmentByTag(FRAGMENT_TAG) != null) return
            FullScreenLoadingDialog().apply {
                arguments = bundleOf(
                    EXTRA_SHOW_CANCEL_BUTTON to showCancel
                )
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
                showJob = launch {
                    delay(MIN_DELAY_MS)
                    delayedShow(showCancel)
                    showJob = null
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
                weakFragmentManager.get()?.let(::closeDialog)
            } else {
                if (!mPostedHide) {
                    hideJob = launch {
                        delay(MIN_SHOW_TIME_MS - diff)
                        delayedHide()
                        hideJob = null
                    }
                    mPostedHide = true
                }
            }
        }

        fun setOnRequestCancelListener(block: () -> Boolean) {
            val manager = weakFragmentManager.get() ?: return
            val owner = weakLifecycleOwner.get() ?: return
            manager.setFragmentResultListener(FRAGMENT_TAG, owner) { _, _ ->
                if (block()) {
                    weakFragmentManager.get()?.let(::closeDialog)
                }
            }
        }
    }
}