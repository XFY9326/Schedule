package tool.xfy9326.schedule.ui.activity

import android.graphics.Bitmap
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.isVisible
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.base.CourseImportConfig
import tool.xfy9326.schedule.content.base.NetworkCourseParser
import tool.xfy9326.schedule.content.base.NetworkCourseProvider
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.databinding.ActivityLoginCourseProviderBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.dialog.ImportCourseConflictDialog
import tool.xfy9326.schedule.ui.vm.NetworkCourseProviderViewModel
import tool.xfy9326.schedule.utils.DialogUtils
import tool.xfy9326.schedule.utils.ViewUtils

class NetworkCourseProviderActivity : ViewModelActivity<NetworkCourseProviderViewModel, ActivityLoginCourseProviderBinding>(),
    ImportCourseConflictDialog.OnConfirmImportCourseConflictListener {
    companion object {
        const val EXTRA_COURSE_IMPORT_CONFIG = "EXTRA_COURSE_IMPORT_CONFIG"
    }

    override fun onPrepare(viewBinding: ActivityLoginCourseProviderBinding, viewModel: NetworkCourseProviderViewModel) {
        setSupportActionBar(viewBinding.toolBarLoginCourseProvider.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.registerConfig(intent.getSerializableExtra(EXTRA_COURSE_IMPORT_CONFIG)
            ?.tryCast<CourseImportConfig<NetworkCourseProvider, NetworkCourseParser>>()!!)
        supportActionBar?.setTitle(if (viewModel.isLoginCourseProvider()) R.string.login_to_import_course else R.string.direct_import_course)
    }

    override fun onBindLiveData(viewBinding: ActivityLoginCourseProviderBinding, viewModel: NetworkCourseProviderViewModel) {
        viewModel.providerError.observeEvent(this, observer = ::showCourseAdapterError)
        viewModel.loginParams.observeEvent(this, observer = ::applyLoginParams)
        viewModel.refreshCaptcha.observeEvent(this, observer = ::setCaptcha)
        viewModel.courseImportFinish.observeEvent(this) {
            if (it.first) {
                afterSaveCourse(it.second)
            } else {
                viewBinding.layoutCourseImportContent.setAllEnable(true)
                viewModel.initLoginParams()
            }
        }
    }

    override fun onInitView(viewBinding: ActivityLoginCourseProviderBinding, viewModel: NetworkCourseProviderViewModel) {
        if (!viewModel.isImportingCourses.get()) {
            viewModel.initLoginParams()
        }
        viewBinding.textViewCourseAdapterSchool.apply {
            isSelected = true
            text = viewModel.importConfig.getSchoolNameText(this@NetworkCourseProviderActivity)
        }
        viewBinding.textViewCourseAdapterSystem.apply {
            isSelected = true
            text = viewModel.importConfig.getSystemNameText(this@NetworkCourseProviderActivity)
        }
        viewBinding.textViewCourseAdapterAuthor.text = getString(R.string.adapter_author, viewModel.importConfig.getAuthorNameText(this))
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
            importCourseToCurrentSchedule()
        }
        viewBinding.buttonImportCourseToNewSchedule.setOnClickListener {
            importCourseToNewSchedule()
        }
    }

    override fun onBackPressed() {
        requireViewBinding().layoutCourseImportContent.clearFocus()

        if (requireViewModel().isImportingCourses.get()) {
            DialogUtils.showCancelScheduleImportDialog(this) {
                requireViewModel().finishImport()
                finish()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun afterSaveCourse(hasConflict: Boolean) {
        showShortToast(R.string.course_import_success)
        if (hasConflict) {
            ImportCourseConflictDialog.showDialog(supportFragmentManager)
        } else {
            finish()
        }
    }

    override fun onConfirmImportCourseConflict() {
        finish()
    }

    private fun importCourseToCurrentSchedule() {
        validateImportCourseParams()?.let {
            DialogUtils.showOverwriteScheduleAttentionDialog(this) {
                importCourse(it, true)
            }
        }
    }

    private fun importCourseToNewSchedule() {
        validateImportCourseParams()?.let { params ->
            DialogUtils.showNewScheduleNameDialog(this) {
                importCourse(params, false, it)
            }
        }
    }

    private fun importCourse(importParams: NetworkCourseProviderViewModel.ImportParams, currentSchedule: Boolean, newScheduleName: String? = null) {
        requireViewBinding().apply {
            layoutCourseImportContent.setAllEnable(false)
            progressBarLoadingCourseImportInit.isIndeterminate = true
            buttonCourseImportReload.isVisible = false
            layoutCourseImportLoading.isVisible = true
            layoutCourseImportContent.isVisible = false
        }
        requireViewModel().importCourse(importParams, currentSchedule, newScheduleName)
    }

    private fun validateImportCourseParams(): NetworkCourseProviderViewModel.ImportParams? {
        requireViewBinding().apply {
            layoutCourseImportContent.clearFocus()

            if (requireViewModel().isLoginCourseProvider()) {
                val userId = editTextUserId.text.getText().let {
                    if (it == null) {
                        layoutLoginCourseProvider.showShortSnackBar(R.string.user_id_empty)
                        return null
                    } else {
                        it
                    }
                }
                val userPw = editTextUserPw.text.getText().let {
                    if (it == null) {
                        layoutLoginCourseProvider.showShortSnackBar(R.string.user_pw_empty)
                        return null
                    } else {
                        it
                    }
                }
                val captchaCode = if (layoutCaptcha.isVisible) {
                    editTextCaptcha.text.getText().also {
                        if (it == null) {
                            layoutLoginCourseProvider.showShortSnackBar(R.string.captcha_empty)
                            return null
                        }
                    }
                } else {
                    null
                }
                val option = getCurrentOption() ?: return null
                return NetworkCourseProviderViewModel.ImportParams(userId, userPw, captchaCode, option)
            } else {

                val option = getCurrentOption() ?: return null
                return NetworkCourseProviderViewModel.ImportParams(null, null, null, option)
            }
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

    private fun applyLoginParams(params: NetworkCourseProviderViewModel.LoginParams?) {
        requireViewBinding().apply {
            if (params != null) {
                when {
                    params.optionsRes != null -> {
                        val adapter = ArrayAdapter(
                            this@NetworkCourseProviderActivity,
                            android.R.layout.simple_list_item_1,
                            getStringArray(params.optionsRes)
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        spinnerCourseImportOptions.adapter = adapter
                        spinnerCourseImportOptions.setSelection(0)
                        layoutImportOptions.isVisible = true
                    }
                    params.optionsOnline != null -> {
                        val adapter = ArrayAdapter(
                            this@NetworkCourseProviderActivity,
                            android.R.layout.simple_list_item_1,
                            params.optionsOnline
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        spinnerCourseImportOptions.adapter = adapter
                        spinnerCourseImportOptions.setSelection(0)
                        layoutImportOptions.isVisible = true
                    }
                    else -> layoutImportOptions.isVisible = false
                }

                layoutUserId.isVisible = params.allowLogin
                layoutUserPw.isVisible = params.allowLogin

                if (params.captcha != null && params.allowLogin) {
                    setCaptcha(params.captcha)
                    layoutCaptcha.isVisible = true
                } else {
                    layoutCaptcha.isVisible = false
                }

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

    private fun setCaptcha(captcha: Bitmap?) {
        if (captcha == null) {
            requireViewBinding().imageViewCaptcha.setImageResource(R.drawable.ic_broken_image_24)
        } else {
            requireViewBinding().imageViewCaptcha.setImageBitmap(captcha)
        }
    }

    private fun showCourseAdapterError(exception: CourseAdapterException) {
        ViewUtils.showCourseAdapterError(this, requireViewBinding().layoutLoginCourseProvider, exception)
    }
}