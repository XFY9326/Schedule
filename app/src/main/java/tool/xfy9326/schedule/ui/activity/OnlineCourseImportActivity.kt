package tool.xfy9326.schedule.ui.activity

import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.databinding.ActivityOnlineCourseImportBinding
import tool.xfy9326.schedule.kt.show
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.tools.livedata.observeNotify
import tool.xfy9326.schedule.ui.activity.base.CourseProviderActivity
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.CourseImportAdapter
import tool.xfy9326.schedule.ui.recyclerview.AdvancedDividerItemDecoration
import tool.xfy9326.schedule.ui.vm.OnlineCourseImportViewModel
import tool.xfy9326.schedule.utils.schedule.CourseImportUtils

class OnlineCourseImportActivity : ViewModelActivity<OnlineCourseImportViewModel, ActivityOnlineCourseImportBinding>(),
    CourseImportAdapter.OnCourseImportItemListener {
    private lateinit var courseImportAdapter: CourseImportAdapter

    override val vmClass = OnlineCourseImportViewModel::class

    override fun onCreateViewBinding() = ActivityOnlineCourseImportBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityOnlineCourseImportBinding, viewModel: OnlineCourseImportViewModel) {
        setSupportActionBar(viewBinding.toolBarCourseImport)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch { if (!AppSettingsDataStore.enableOnlineCourseImportFlow.first()) finish() }
    }

    override fun onBindLiveData(viewBinding: ActivityOnlineCourseImportBinding, viewModel: OnlineCourseImportViewModel) {
        viewModel.onlineImportAttention.observeNotify(this) {
            showOnlineImportAttentionDialog(true)
        }
        viewModel.sortedConfigs.observe(this) {
            courseImportAdapter.submitList(it)
        }
    }

    override fun onInitView(viewBinding: ActivityOnlineCourseImportBinding, viewModel: OnlineCourseImportViewModel) {
        courseImportAdapter = CourseImportAdapter()
        courseImportAdapter.setOnCourseImportItemListener(this)
        viewBinding.recyclerViewCourseImportList.adapter = courseImportAdapter
        viewBinding.recyclerViewCourseImportList.addItemDecoration(AdvancedDividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_online_course_import, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_onlineCourseImportAttention) {
            showOnlineImportAttentionDialog(false)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showOnlineImportAttentionDialog(enableControls: Boolean) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.online_course_import)
            setMessage(R.string.online_course_import_attention)
            if (enableControls) {
                setCancelable(false)
                setPositiveButton(R.string.has_read) { _, _ ->
                    requireViewModel().hasReadOnlineImportAttention()
                }
                setNegativeButton(android.R.string.cancel) { _, _ ->
                    finish()
                }
                setNeutralButton(R.string.disable_function) { _, _ ->
                    lifecycleScope.launch {
                        AppSettingsDataStore.setEnableOnlineCourseImportFlow(false)
                        finish()
                    }
                }
            }
        }.show(this)
    }

    private fun openCourseImportActivity(config: AbstractCourseImportConfig<*, *, *, *>) {
        val importMethod = CourseImportUtils.getCourseImportMethod(config,
            onInterfaceProviderError = {
                requireViewBinding().layoutCourseImport.showShortSnackBar(R.string.interface_provider_error)
            },
            onInvalidParser = {
                requireViewBinding().layoutCourseImport.showShortSnackBar(R.string.invalid_parser_error)
            },
            onUnknownProviderError = {
                requireViewBinding().layoutCourseImport.showShortSnackBar(R.string.unknown_provider_error)
            }
        )
        when (importMethod) {
            CourseImportUtils.ImportMethod.LOGIN_IMPORT, CourseImportUtils.ImportMethod.NETWORK_IMPORT ->
                CourseProviderActivity.startProviderActivity<NetworkCourseProviderActivity>(this, config)
            CourseImportUtils.ImportMethod.WEB_IMPORT -> CourseProviderActivity.startProviderActivity<WebCourseProviderActivity>(this, config)
            CourseImportUtils.ImportMethod.WEB_JS_IMPORT -> TODO("Add Activity")
        }
    }

    override fun onCourseImportConfigClick(config: AbstractCourseImportConfig<*, *, *, *>) {
        openCourseImportActivity(config)
    }

    override fun onJSConfigClick(jsConfig: JSConfig) {
        TODO("Not yet implemented")
    }

    override fun onJSConfigDelete(jsConfig: JSConfig) {
        TODO("Not yet implemented")
    }
}