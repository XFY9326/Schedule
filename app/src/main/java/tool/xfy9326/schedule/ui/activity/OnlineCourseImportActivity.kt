package tool.xfy9326.schedule.ui.activity

import androidx.recyclerview.widget.DividerItemDecoration
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.base.CourseImportConfig
import tool.xfy9326.schedule.databinding.ActivityOnlineCourseImportBinding
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.CourseImportAdapter
import tool.xfy9326.schedule.ui.recyclerview.AdvancedDividerItemDecoration
import tool.xfy9326.schedule.ui.vm.CourseImportViewModel
import tool.xfy9326.schedule.utils.CourseImportUtils

class OnlineCourseImportActivity : ViewModelActivity<CourseImportViewModel, ActivityOnlineCourseImportBinding>() {
    private lateinit var courseImportAdapter: CourseImportAdapter

    override fun onPrepare(viewBinding: ActivityOnlineCourseImportBinding, viewModel: CourseImportViewModel) {
        setSupportActionBar(viewBinding.toolBarCourseImport)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.loadCourseImportMetas()
        courseImportAdapter = CourseImportAdapter()
    }

    override fun onBindLiveData(viewBinding: ActivityOnlineCourseImportBinding, viewModel: CourseImportViewModel) {
        viewModel.courseMetas.observe(this, courseImportAdapter::updateList)
    }

    override fun onInitView(viewBinding: ActivityOnlineCourseImportBinding, viewModel: CourseImportViewModel) {
        courseImportAdapter.setOnCourseImportItemClickListener(::onCourseImport)
        viewBinding.recyclerViewCourseImportList.adapter = courseImportAdapter
        viewBinding.recyclerViewCourseImportList.addItemDecoration(AdvancedDividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun onCourseImport(config: CourseImportConfig<*, *>) {
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
                startActivity<NetworkCourseProviderActivity> {
                    putExtra(NetworkCourseProviderActivity.EXTRA_COURSE_IMPORT_CONFIG, config)
                }
            CourseImportUtils.ImportMethod.WEB_IMPORT -> startActivity<WebCourseProviderActivity> {
                putExtra(WebCourseProviderActivity.EXTRA_COURSE_IMPORT_CONFIG, config)
            }
        }
    }
}