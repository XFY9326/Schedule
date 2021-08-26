package tool.xfy9326.schedule.ui.activity

import android.graphics.Bitmap
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import coil.load
import lib.xfy9326.android.kit.getText
import lib.xfy9326.android.kit.setAllEnable
import lib.xfy9326.android.kit.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.NetworkCourseImportParams
import tool.xfy9326.schedule.beans.NetworkProviderParams
import tool.xfy9326.schedule.beans.ScheduleImportRequestParams
import tool.xfy9326.schedule.content.base.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.databinding.ActivityNetworkCourseProviderBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.ui.activity.base.CourseProviderActivity
import tool.xfy9326.schedule.ui.vm.NetworkCourseProviderViewModel
import tool.xfy9326.schedule.utils.schedule.ScheduleImportManager
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

        viewModel.loginCaptcha.observe(this, ::setCaptcha)
        viewModel.providerParams.observe(this, ::applyProviderParams)
        viewModel.importOptions.observe(this, ::setupOptions)
    }

    override fun onInitView(viewBinding: ActivityNetworkCourseProviderBinding, viewModel: NetworkCourseProviderViewModel) {
        viewBinding.textViewCourseAdapterSchool.apply {
            isSelected = true
            text = viewModel.importConfigInstance.schoolName
        }
        viewBinding.textViewCourseAdapterSystem.apply {
            isSelected = true
            text = viewModel.importConfigInstance.systemName
        }
        viewBinding.textViewCourseAdapterAuthor.text = getString(R.string.adapter_author, viewModel.importConfigInstance.authorName)
        viewBinding.imageViewCaptcha.setOnSingleClickListener {
            if (!viewModel.isImportingCourses) {
                withImportOption {
                    viewModel.refreshCaptcha(it)
                }
            }
        }
        viewBinding.buttonCourseImportReload.setOnSingleClickListener {
            if (!viewModel.isImportingCourses) {
                withImportOption {
                    setLoadingView(isLoading = true, isError = false)
                    viewModel.refreshLoginPageInfo(it)
                }
            }
        }

        viewBinding.buttonImportCourseToCurrentSchedule.setOnSingleClickListener {
            withImportOption {
                withImportCourseParams { params ->
                    requestImportCourse(ScheduleImportRequestParams(true, params, it))
                }
            }
        }
        viewBinding.buttonImportCourseToNewSchedule.setOnSingleClickListener {
            withImportOption {
                withImportCourseParams { params ->
                    requestImportCourse(ScheduleImportRequestParams(false, params, it))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_network_course_provider, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_networkCourseProviderRefresh -> {
                if (!requireViewModel().isImportingCourses) {
                    withImportOption {
                        setLoadingView(isLoading = true, isError = false)
                        requireViewModel().refreshLoginPageInfo(it)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
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
        ViewUtils.showCourseImportErrorSnackBar(this, requireViewBinding().layoutLoginCourseProvider, exception)
    }

    override fun onCourseImportFinish(result: ScheduleImportManager.ImportResult, editScheduleId: Long?) {
        if (result == ScheduleImportManager.ImportResult.FAILED) {
            requireViewBinding().layoutCourseImportContent.setAllEnable(true)
            withImportOption(false) {
                requireViewModel().refreshLoginPageInfo(it)
            }
        }
    }

    override fun onReadyImportCourse() {
        requireViewBinding().apply {
            layoutCourseImportContent.setAllEnable(false)
            setLoadingView(isLoading = true, isError = false)
        }
    }

    private fun withImportCourseParams(block: (NetworkCourseImportParams) -> Unit) {
        requireViewBinding().apply {
            layoutCourseImportContent.clearFocus()
            if (requireViewModel().isLoginCourseProvider) {
                val userId = getTextWithCheck(editTextUserId, R.string.user_id_empty) ?: return
                val userPw = getTextWithCheck(editTextUserPw, R.string.user_pw_empty) ?: return
                val captchaCode = (if (layoutCaptcha.isVisible) getTextWithCheck(editTextCaptcha, R.string.captcha_empty) else null) ?: return

                block(NetworkCourseImportParams(userId, userPw, captchaCode))
            }
        }
    }

    private fun getTextWithCheck(editText: EditText, @StringRes errorMsg: Int) =
        editText.text.getText().let {
            if (it == null) {
                requireViewBinding().layoutLoginCourseProvider.showSnackBar(errorMsg)
                null
            } else {
                it
            }
        }

    private fun withImportOption(warning: Boolean = true, block: (Int) -> Unit) {
        requireViewBinding().apply {
            if (layoutImportOptions.isVisible) {
                spinnerCourseImportOptions.selectedItemPosition.also {
                    if (it == Spinner.INVALID_POSITION) {
                        if (warning) layoutLoginCourseProvider.showSnackBar(R.string.options_empty)
                    } else {
                        block(it)
                    }
                }
            } else {
                block(0)
            }
        }
    }

    private fun applyProviderParams(params: NetworkProviderParams?) {
        requireViewBinding().apply {
            if (params != null) {
                layoutUserId.isVisible = params.enableLogin
                layoutUserPw.isVisible = params.enableLogin
                layoutCaptcha.isVisible = params.enableCaptcha
                if (params.enableCaptcha) {
                    editTextCaptcha.text = null
                }
                setLoadingView(isLoading = false, isError = false)
            } else {
                setLoadingView(isLoading = false, isError = true)
            }
        }
    }

    private fun setLoadingView(isLoading: Boolean, isError: Boolean) {
        requireViewBinding().apply {
            when {
                isError -> {
                    layoutCourseImportLoading.isVisible = true
                    layoutCourseImportContent.isVisible = false

                    progressBarLoadingCourseImportInit.hide()
                    buttonCourseImportReload.isVisible = true
                    imageViewCourseImportLoadError.isVisible = true
                }
                isLoading -> {
                    layoutCourseImportLoading.isVisible = true
                    layoutCourseImportContent.isVisible = false

                    progressBarLoadingCourseImportInit.show()
                    buttonCourseImportReload.isVisible = false
                    imageViewCourseImportLoadError.isVisible = false
                }
                else -> {
                    layoutCourseImportLoading.isVisible = false
                    layoutCourseImportContent.isVisible = true
                }
            }
        }
    }

    private fun setupOptions(options: Array<String>?) {
        requireViewBinding().apply {
            if (options == null) {
                layoutImportOptions.isVisible = false
            } else {
                val adapter = ArrayAdapter(
                    this@NetworkCourseProviderActivity,
                    android.R.layout.simple_list_item_1,
                    options
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerCourseImportOptions.adapter = adapter
                if (spinnerCourseImportOptions.selectedItemPosition == Spinner.INVALID_POSITION) {
                    spinnerCourseImportOptions.setSelection(0)
                }
                layoutImportOptions.isVisible = true
            }
        }
    }

    private fun setCaptcha(captcha: Bitmap?) {
        requireViewBinding().imageViewCaptcha.load(captcha) {
            error(R.drawable.ic_broken_image_24)
        }
    }
}