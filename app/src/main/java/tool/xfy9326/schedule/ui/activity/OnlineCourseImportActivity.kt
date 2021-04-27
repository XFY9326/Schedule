package tool.xfy9326.schedule.ui.activity

import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lib.xfy9326.livedata.observeEvent
import lib.xfy9326.livedata.observeNotify
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.databinding.ActivityOnlineCourseImportBinding
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.ui.activity.base.CourseProviderActivity
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.CourseImportAdapter
import tool.xfy9326.schedule.ui.dialog.FullScreenLoadingDialog
import tool.xfy9326.schedule.ui.dialog.JSConfigImportDialog
import tool.xfy9326.schedule.ui.dialog.JSConfigPrepareDialog
import tool.xfy9326.schedule.ui.recyclerview.AdvancedDividerItemDecoration
import tool.xfy9326.schedule.ui.vm.OnlineCourseImportViewModel
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.schedule.CourseImportUtils
import tool.xfy9326.schedule.utils.view.DialogUtils
import tool.xfy9326.schedule.utils.view.ViewUtils

class OnlineCourseImportActivity : ViewModelActivity<OnlineCourseImportViewModel, ActivityOnlineCourseImportBinding>(),
    CourseImportAdapter.OnCourseImportItemListener, JSConfigImportDialog.OnJSConfigImportListener, FullScreenLoadingDialog.OnRequestCancelListener {
    private lateinit var courseImportAdapter: CourseImportAdapter
    private val loadingController = FullScreenLoadingDialog.createControllerInstance(this)
    private val selectJSConfig = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            requireViewModel().addJSConfig(it)
        } else {
            requireViewBinding().layoutCourseImport.showSnackBar(R.string.input_file_cancel)
        }
    }

    override val vmClass = OnlineCourseImportViewModel::class

    override fun onCreateViewBinding() = ActivityOnlineCourseImportBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityOnlineCourseImportBinding, viewModel: OnlineCourseImportViewModel) {
        setSupportActionBar(viewBinding.toolBarCourseImport)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch { if (!AppSettingsDataStore.enableOnlineCourseImportFlow.first()) finish() }
    }

    override fun onBindLiveData(viewBinding: ActivityOnlineCourseImportBinding, viewModel: OnlineCourseImportViewModel) {
        viewModel.onlineImportAttention.observeNotify(this) {
            DialogUtils.showOnlineImportAttentionDialog(this, true,
                onPositive = {
                    requireViewModel().hasReadOnlineImportAttention()
                },
                onNegative = {
                    finish()
                },
                onNeutral = {
                    lifecycleScope.launch {
                        AppSettingsDataStore.setEnableOnlineCourseImportFlow(false)
                        finish()
                    }
                })
        }
        viewModel.courseImportConfigs.observe(this) {
            courseImportAdapter.submitList(it)
        }
        viewModel.preparedJSConfig.observeEvent(this) {
            openCourseImportActivity(it)
        }
        viewModel.configOperationAttention.observeEvent(this) {
            if (!JSConfigPrepareDialog.isShowing(supportFragmentManager)) {
                loadingController.hide()
                showAttention(it.getText(this))
            }
        }
        viewModel.configOperationError.observeEvent(this, javaClass.simpleName) {
            if (!JSConfigPrepareDialog.isShowing(supportFragmentManager)) {
                loadingController.hide()
                ViewUtils.showJSConfigErrorSnackBar(this, viewBinding.layoutCourseImport, it)
            }
        }
        viewModel.configIgnorableWarning.observeEvent(this, javaClass.simpleName) {
            if (!JSConfigPrepareDialog.isShowing(supportFragmentManager)) {
                ViewUtils.showJSConfigErrorSnackBar(this, viewBinding.layoutCourseImport, it)
            }
        }
    }

    override fun onInitView(viewBinding: ActivityOnlineCourseImportBinding, viewModel: OnlineCourseImportViewModel) {
        courseImportAdapter = CourseImportAdapter()
        courseImportAdapter.setOnCourseImportItemListener(this)
        viewBinding.recyclerViewCourseImportList.adapter = courseImportAdapter
        viewBinding.recyclerViewCourseImportList.addItemDecoration(AdvancedDividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        viewBinding.fabAddCourseImport.setOnClickListener {
            lifecycleScope.launch {
                if (!AppDataStore.agreeCourseImportPolicyFlow.first()) {
                    DialogUtils.showAddCourseImportAttentionDialog(this@OnlineCourseImportActivity) {
                        lifecycleScope.launch {
                            AppDataStore.setAgreeCourseImportPolicy()
                        }
                        JSConfigImportDialog.showDialog(supportFragmentManager)
                    }
                } else {
                    JSConfigImportDialog.showDialog(supportFragmentManager)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_online_course_import, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_onlineCourseImportAttention) {
            DialogUtils.showOnlineImportAttentionDialog(this, false,
                onNeutral = {
                    IntentUtils.openUrlInBrowser(this, IntentUtils.COURSE_IMPORT_WIKI_URL)
                })
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openCourseImportActivity(config: AbstractCourseImportConfig<*, *, *, *>) {
        val importMethod = CourseImportUtils.getCourseImportMethod(config,
            onInterfaceProviderError = {
                showAttention(R.string.interface_provider_error)
            },
            onInvalidParser = {
                showAttention(R.string.invalid_parser_error)
            },
            onUnknownProviderError = {
                showAttention(R.string.unknown_provider_error)
            }
        )
        when (importMethod) {
            CourseImportUtils.ImportMethod.LOGIN_IMPORT, CourseImportUtils.ImportMethod.NETWORK_IMPORT ->
                CourseProviderActivity.startProviderActivity<NetworkCourseProviderActivity>(this, config)
            CourseImportUtils.ImportMethod.WEB_IMPORT -> CourseProviderActivity.startProviderActivity<WebCourseProviderActivity>(this, config)
            CourseImportUtils.ImportMethod.WEB_JS_IMPORT -> CourseProviderActivity.startProviderActivity<JSCourseProviderActivity>(this, config)
        }
    }

    private fun showAttention(@StringRes id: Int) = requireViewBinding().layoutCourseImport.showSnackBar(id)

    private fun showAttention(msg: String) = requireViewBinding().layoutCourseImport.showSnackBar(msg)

    override fun onCourseImportConfigClick(config: AbstractCourseImportConfig<*, *, *, *>) {
        openCourseImportActivity(config)
    }

    override fun onJSConfigClick(jsConfig: JSConfig) {
        JSConfigPrepareDialog.showDialog(supportFragmentManager, jsConfig)
    }

    override fun onJSConfigDelete(jsConfig: JSConfig) {
        requireViewModel().deleteJSConfig(jsConfig)
    }

    override fun onJSConfigUrlImport(url: String) {
        loadingController.show()
        requireViewModel().addJSConfig(url)
    }

    override fun onJSConfigFileImport() {
        loadingController.show()
        selectJSConfig.launch(MIMEConst.MIME_APPLICATION_JSON)
    }

    override fun onFullScreenLoadingDialogRequestCancel(): Boolean {
        requireViewModel().cancelJSConfigAdd()
        return true
    }
}