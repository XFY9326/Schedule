package tool.xfy9326.schedule.ui.activity

import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import coil.load
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.NetworkCourseImportParams
import tool.xfy9326.schedule.beans.NetworkLoginParams
import tool.xfy9326.schedule.content.base.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.databinding.ActivityNetworkCourseProviderBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.tools.livedata.observeEvent
import tool.xfy9326.schedule.ui.activity.base.CourseProviderActivity
import tool.xfy9326.schedule.ui.vm.NetworkCourseProviderViewModel
import tool.xfy9326.schedule.ui.vm.base.CourseProviderViewModel
import tool.xfy9326.schedule.utils.view.DialogUtils
import tool.xfy9326.schedule.utils.view.ViewUtils

class NetworkCourseProviderActivity :
    CourseProviderActivity<NetworkCourseImportParams, NetworkCourseProvider<*>, NetworkCourseParser<*>, NetworkCourseProviderViewModel, ActivityNetworkCourseProviderBinding>() {

    override val exitIfImportSuccess = true

    override val vmClass = NetworkCourseProviderViewModel::class

    override fun onCreateViewBinding() = ActivityNetworkCourseProviderBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityNetworkCourseProviderBinding, viewModel: NetworkCourseProviderViewModel) {
        super.onPrepare(viewBinding, viewModel)

        setSupportActionBar(viewBinding.toolBarLoginCourseProvider.toolBarGeneral)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(if (viewModel.isLoginCourseProvider) R.string.login_to_import_course else R.string.direct_import_course)
        }
    }

    override fun onBindLiveData(viewBinding: ActivityNetworkCourseProviderBinding, viewModel: NetworkCourseProviderViewModel) {
        super.onBindLiveData(viewBinding, viewModel)

        viewModel.loginParams.observeEvent(this, observer = ::applyLoginParams)
        viewModel.refreshCaptcha.observeEvent(this, observer = ::setCaptcha)
    }

    override fun onInitView(viewBinding: ActivityNetworkCourseProviderBinding, viewModel: NetworkCourseProviderViewModel) {
        if (!viewModel.isImportingCourses) {
            viewModel.initLoginParams()
        }
        viewBinding.textViewCourseAdapterSchool.apply {
            isSelected = true
            text = getString(viewModel.importConfig.schoolNameResId)
        }
        viewBinding.textViewCourseAdapterSystem.apply {
            isSelected = true
            text = getString(viewModel.importConfig.systemNameResId)
        }
        viewBinding.textViewCourseAdapterAuthor.text = getString(R.string.adapter_author, getString(viewModel.importConfig.authorNameResId))
        viewBinding.spinnerCourseImportOptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.refreshCaptcha(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        viewBinding.imageViewCaptcha.setOnClickListener {
            viewModel.refreshCaptcha(viewBinding.spinnerCourseImportOptions.selectedItemPosition)
        }
        viewBinding.buttonCourseImportReload.setOnClickListener {
            viewBinding.progressBarLoadingCourseImportInit.isIndeterminate = true
            it.isVisible = false
            viewModel.initLoginParams()
        }

        viewBinding.buttonImportCourseToCurrentSchedule.setOnClickListener {
            getCurrentOption()?.let {
                requestImportCourse(ImportRequestParams(true, getImportCourseParams(), it))
            }
        }
        viewBinding.buttonImportCourseToNewSchedule.setOnClickListener {
            getCurrentOption()?.let {
                requestImportCourse(ImportRequestParams(false, getImportCourseParams(), it))
            }
        }
    }

    override fun onBackPressed() {
        requireViewBinding().layoutCourseImportContent.clearFocus()

        if (requireViewModel().isImportingCourses) {
            DialogUtils.showCancelScheduleImportDialog(this) {
                requireViewModel().finishImport()
                finish()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onShowCourseAdapterError(exception: CourseAdapterException) {
        ViewUtils.showCourseAdapterErrorSnackBar(this, requireViewBinding().layoutLoginCourseProvider, exception)
    }

    override fun onCourseImportFinish(result: CourseProviderViewModel.ImportResult, editScheduleId: Long?) {
        if (result == CourseProviderViewModel.ImportResult.FAILED) {
            requireViewBinding().layoutCourseImportContent.setAllEnable(true)
            requireViewModel().initLoginParams()
        }
    }

    override fun onReadyImportCourse() {
        requireViewBinding().apply {
            layoutCourseImportContent.setAllEnable(false)
            progressBarLoadingCourseImportInit.isIndeterminate = true
            buttonCourseImportReload.isVisible = false
            layoutCourseImportLoading.isVisible = true
            layoutCourseImportContent.isVisible = false
        }
    }

    private fun getImportCourseParams(): NetworkCourseImportParams {
        requireViewBinding().apply {
            layoutCourseImportContent.clearFocus()

            return if (requireViewModel().isLoginCourseProvider) {
                val userId = getTextWithCheck(editTextUserId, R.string.user_id_empty)
                val userPw = getTextWithCheck(editTextUserPw, R.string.user_pw_empty)
                val captchaCode = if (layoutCaptcha.isVisible) {
                    getTextWithCheck(editTextCaptcha, R.string.captcha_empty)
                } else {
                    null
                }
                NetworkCourseImportParams(userId, userPw, captchaCode)
            } else {
                NetworkCourseImportParams(null, null, null)
            }
        }
    }

    private fun getTextWithCheck(editText: EditText, @StringRes errorMsg: Int) =
        editText.text.getText().let {
            if (it == null) {
                requireViewBinding().layoutLoginCourseProvider.showShortSnackBar(errorMsg)
                null
            } else {
                it
            }
        }

    private fun getCurrentOption(): Int? {
        requireViewBinding().apply {
            return if (layoutImportOptions.isVisible) {
                spinnerCourseImportOptions.selectedItemPosition.also {
                    if (it == Spinner.INVALID_POSITION) {
                        layoutLoginCourseProvider.showShortSnackBar(R.string.options_empty)
                        return null
                    }
                }
            } else {
                0
            }
        }
    }

    private fun applyLoginParams(params: NetworkLoginParams?) {
        requireViewBinding().apply {
            if (params != null) {
                if (params.options == null) {
                    layoutImportOptions.isVisible = false
                } else {
                    setupOptions(params.options)
                }

                layoutUserId.isVisible = params.allowLogin
                layoutUserPw.isVisible = params.allowLogin
                layoutCaptcha.isVisible = params.enableCaptcha

                layoutCourseImportLoading.isVisible = false
                layoutCourseImportContent.isVisible = true
            } else {
                progressBarLoadingCourseImportInit.isIndeterminate = false
                buttonCourseImportReload.isVisible = true

                layoutCourseImportLoading.isVisible = true
                layoutCourseImportContent.isVisible = false
            }
        }
    }

    private fun setupOptions(array: Array<String>) {
        requireViewBinding().apply {
            val adapter = ArrayAdapter(
                this@NetworkCourseProviderActivity,
                android.R.layout.simple_list_item_1,
                array
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            spinnerCourseImportOptions.adapter = adapter
            spinnerCourseImportOptions.setSelection(0)
            layoutImportOptions.isVisible = true
        }
    }

    private fun setCaptcha(captcha: BitmapDrawable?) {
        requireViewBinding().imageViewCaptcha.load(captcha) {
            error(R.drawable.ic_broken_image_24)
        }
    }
}