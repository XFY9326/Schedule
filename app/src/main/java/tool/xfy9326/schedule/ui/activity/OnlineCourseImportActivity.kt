package tool.xfy9326.schedule.ui.activity

import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.livedata.observeNotify
import io.github.xfy9326.atools.ui.openUrlInBrowser
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import io.github.xfy9326.atools.ui.show
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.CourseImportConfigManager.Type.Companion.getText
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.utils.BaseCourseImportConfig
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
import tool.xfy9326.schedule.ui.view.recyclerview.AdvancedDividerItemDecoration
import tool.xfy9326.schedule.ui.vm.OnlineCourseImportViewModel
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.schedule.CourseImportUtils
import tool.xfy9326.schedule.utils.view.DialogUtils
import tool.xfy9326.schedule.utils.view.ViewUtils

class OnlineCourseImportActivity : ViewModelActivity<OnlineCourseImportViewModel, ActivityOnlineCourseImportBinding>(), CourseImportAdapter.OnCourseImportItemListener {
    private lateinit var courseImportAdapter: CourseImportAdapter
    private val loadingController by lazy { FullScreenLoadingDialog.Controller.newInstance(this, supportFragmentManager) }
    private val selectJSConfig = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            requireViewModel().addJSConfig(it)
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
        viewModel.preparedJSConfig.observeEvent(this, javaClass.simpleName) {
            openCourseImportActivity(it)
        }
        viewModel.configOperationAttention.observeEvent(this, javaClass.simpleName) {
            if (!JSConfigPrepareDialog.isShowing(supportFragmentManager)) {
                loadingController.hide()
                showAttention(it.getText(this))
            }
        }
        viewModel.configOperationError.observeEvent(this, javaClass.simpleName) {
            if (!JSConfigPrepareDialog.isShowing(supportFragmentManager)) {
                loadingController.hide()
                ViewUtils.showCourseImportErrorSnackBar(this, viewBinding.layoutCourseImport, it)
            }
        }
        viewModel.configIgnorableWarning.observeEvent(this, javaClass.simpleName) {
            if (!JSConfigPrepareDialog.isShowing(supportFragmentManager)) {
                ViewUtils.showCourseImportErrorSnackBar(this, viewBinding.layoutCourseImport, it)
            }
        }
        viewModel.jsConfigExistWarning.observeEvent(this, javaClass.simpleName) {
            showJSConfigExistWarningDialog(it.first, it.second)
        }
        viewModel.launchJSConfig.observeEvent(this, javaClass.simpleName) {
            if (it.first.requireNetwork && !it.second) {
                showJSRequireNetworkWarning()
            } else {
                JSConfigPrepareDialog.showDialog(supportFragmentManager, it.first)
            }
        }
    }

    private fun showJSRequireNetworkWarning() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.js_course_import_enable_network_title)
            setMessage(R.string.js_course_import_enable_network_msg)
            setPositiveButton(android.R.string.ok, null)
        }.show(this)
    }

    override fun onInitView(viewBinding: ActivityOnlineCourseImportBinding, viewModel: OnlineCourseImportViewModel) {
        courseImportAdapter = CourseImportAdapter()
        courseImportAdapter.setOnCourseImportItemListener(this)
        viewBinding.recyclerViewCourseImportList.adapter = courseImportAdapter
        viewBinding.recyclerViewCourseImportList.addItemDecoration(AdvancedDividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        viewBinding.fabAddCourseImport.setOnSingleClickListener {
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
        loadingController.setOnRequestCancelListener {
            viewModel.cancelJSConfigAdd()
            true
        }
        JSConfigImportDialog.setOnJSConfigImportListener(supportFragmentManager, this,
            onUrlImport = {
                loadingController.show()
                requireViewModel().addJSConfig(it)
            },
            onFileImport = {
                selectJSConfig.launch(MIMEConst.MIME_APPLICATION_JSON)
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_online_course_import, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_onlineCourseImportAttention) {
            DialogUtils.showOnlineImportAttentionDialog(this, false,
                onNeutral = {
                    openUrlInBrowser(IntentUtils.COURSE_IMPORT_WIKI_URL.toUri())
                })
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showJSConfigExistWarningDialog(importConfig: JSConfig, existConfig: JSConfig) {
        loadingController.hide()
        val updateUrlChanged = importConfig.updateUrl != existConfig.updateUrl
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.js_config_exist_title)
            .setMessage(
                getString(
                    R.string.js_config_exist_msg,
                    importConfig.schoolName,
                    importConfig.authorName,
                    existConfig.schoolName,
                    existConfig.authorName,
                    if (updateUrlChanged) getString(R.string.js_config_update_url_changed) else getString(R.string.js_config_update_url_not_changed)
                )
            )
            .setPositiveButton(android.R.string.ok) { _, _ ->
                requireViewModel().forceAddJSConfig(importConfig)
                loadingController.show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show(this)
    }

    private fun openCourseImportActivity(config: BaseCourseImportConfig) {
        CourseImportUtils.getCourseImportMethod(config,
            onInterfaceProviderError = {
                showAttention(R.string.interface_provider_error)
            },
            onInvalidParser = {
                showAttention(R.string.invalid_parser_error)
            },
            onUnknownProviderError = {
                showAttention(R.string.unknown_provider_error)
            }
        )?.let {
            when (it) {
                CourseImportUtils.ImportMethod.LOGIN_IMPORT, CourseImportUtils.ImportMethod.NETWORK_IMPORT ->
                    CourseProviderActivity.startProviderActivity<NetworkCourseProviderActivity>(this, config)
                CourseImportUtils.ImportMethod.WEB_IMPORT -> CourseProviderActivity.startProviderActivity<WebCourseProviderActivity>(this, config)
                CourseImportUtils.ImportMethod.WEB_JS_IMPORT -> CourseProviderActivity.startProviderActivity<JSCourseProviderActivity>(this, config)
            }
        }
    }

    private fun showAttention(@StringRes id: Int) = requireViewBinding().layoutCourseImport.showSnackBar(id)

    private fun showAttention(msg: String) = requireViewBinding().layoutCourseImport.showSnackBar(msg)

    override fun onCourseImportConfigClick(config: BaseCourseImportConfig) {
        openCourseImportActivity(config)
    }

    override fun onJSConfigClick(jsConfig: JSConfig) {
        requireViewModel().launchJSConfig(jsConfig)
    }

    override fun onJSConfigDelete(jsConfig: JSConfig) {
        requireViewModel().deleteJSConfig(jsConfig)
    }
}