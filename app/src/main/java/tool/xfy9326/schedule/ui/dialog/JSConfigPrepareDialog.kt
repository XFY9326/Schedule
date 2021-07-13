package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.CourseImportConfigManager
import tool.xfy9326.schedule.content.CourseImportConfigManager.Type.*
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.utils.JSConfigException
import tool.xfy9326.schedule.databinding.DialogJsConfigPrepareBinding
import tool.xfy9326.schedule.kt.getDeepStackTraceString
import tool.xfy9326.schedule.kt.setWindowPercent
import tool.xfy9326.schedule.kt.showToast
import tool.xfy9326.schedule.ui.vm.OnlineCourseImportViewModel

class JSConfigPrepareDialog : DialogFragment() {
    companion object {
        private val DIALOG_TAG = JSConfigPrepareDialog::class.simpleName
        private const val EXTRA_JS_CONFIG = "EXTRA_JS_CONFIG"
        private const val EXTRA_PREPARE_RUNNING = "EXTRA_PREPARE_RUNNING"
        private const val WINDOW_WIDTH_PERCENT = 0.6

        fun showDialog(fragmentManager: FragmentManager, jsConfig: JSConfig) {
            JSConfigPrepareDialog().apply {
                arguments = bundleOf(
                    EXTRA_JS_CONFIG to jsConfig
                )
            }.show(fragmentManager, DIALOG_TAG)
        }

        fun isShowing(fragmentManager: FragmentManager) = (fragmentManager.findFragmentByTag(DIALOG_TAG) != null)
    }

    private val viewModel by activityViewModels<OnlineCourseImportViewModel>()
    private lateinit var jsConfig: JSConfig
    private lateinit var binding: DialogJsConfigPrepareBinding
    private var isRunning = false
    private var lastRunException: JSConfigException? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jsConfig = requireArguments().getParcelable(EXTRA_JS_CONFIG)!!
        isRunning = savedInstanceState?.getBoolean(EXTRA_PREPARE_RUNNING, false) ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(EXTRA_PREPARE_RUNNING, isRunning)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogJsConfigPrepareBinding.inflate(inflater, container, false)

        binding.buttonConfigPrepareClose.setOnClickListener {
            viewModel.cancelPrepareJSConfig()
            dismiss()
        }
        binding.buttonConfigPrepareReload.setOnClickListener {
            runPreparing()
        }
        binding.textViewConfigPrepareErrorDetail.setOnClickListener {
            val exception = lastRunException
            if (exception == null) {
                showToast(R.string.crash_detail_not_found)
            } else {
                CrashViewDialog.showDialog(childFragmentManager, exception.getDeepStackTraceString())
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.jsConfigPrepareProgress.observeEvent(this, javaClass.simpleName) {
            when (it) {
                CHECK_UPDATE, PREPARE_PROVIDER, PREPARE_PARSER, PREPARE_DEPENDENCIES -> updateProgressText(it)
                PREPARE_FINISH -> {
                    isRunning = false
                    requireDialog().setCancelable(true)
                    dismiss()
                }
                else -> {
                    // ignore
                }
            }
        }
        viewModel.configOperationError.observeEvent(this, javaClass.simpleName) {
            isRunning = false
            setShowView(true)
            lastRunException = it
            showToast(it.getText(requireContext()))
            requireDialog().setCancelable(true)
        }
        viewModel.configIgnorableWarning.observeEvent(this, javaClass.simpleName) {
            showToast(it.getText(requireContext()))
        }

        runPreparing()
    }

    override fun onStart() {
        super.onStart()
        requireDialog().setWindowPercent(WINDOW_WIDTH_PERCENT)
    }

    private fun runPreparing() {
        if (!isRunning) {
            isRunning = true
            requireDialog().setCancelable(false)
            lastRunException = null
            setShowView(false)
            viewModel.prepareJSConfig(jsConfig)
        }
    }

    private fun setShowView(isError: Boolean) {
        binding.textViewConfigPrepareProgress.isVisible = !isError
        binding.layoutConfigPrepareReload.isVisible = isError

        binding.progressBarConfigPrepare.isVisible = !isError
        binding.imageViewConfigPrepareFailed.isVisible = isError
    }

    private fun updateProgressText(type: CourseImportConfigManager.Type) {
        binding.textViewConfigPrepareProgress.text = type.getText(requireContext())
    }
}