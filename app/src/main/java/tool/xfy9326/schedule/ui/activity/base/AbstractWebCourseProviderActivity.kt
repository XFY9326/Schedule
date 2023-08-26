package tool.xfy9326.schedule.ui.activity.base

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.commitNow
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.base.AbstractCourseProvider
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.databinding.ActivityFragmentContainerBinding
import tool.xfy9326.schedule.ui.fragment.WebCourseProviderFragment
import tool.xfy9326.schedule.ui.fragment.base.IWebCourseProvider
import tool.xfy9326.schedule.ui.vm.base.AbstractWebCourseProviderViewModel
import tool.xfy9326.schedule.utils.view.ViewUtils

abstract class AbstractWebCourseProviderActivity<I, P1 : AbstractCourseProvider<*>, P2 : AbstractCourseParser<*>, M : AbstractWebCourseProviderViewModel<I, P1, P2>> :
    CourseProviderActivity<I, P1, P2, M, ActivityFragmentContainerBinding>(), IWebCourseProvider.IActivityContact {
    private lateinit var iFragmentContact: IWebCourseProvider.IFragmentContact
    protected val fragmentContact
        get() = iFragmentContact

    override val exitIfImportSuccess = false

    @CallSuper
    override fun onContentViewPreload(savedInstanceState: Bundle?, viewModel: M) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    final override fun onCreateViewBinding() = ActivityFragmentContainerBinding.inflate(layoutInflater)

    @CallSuper
    override fun onPrepare(viewBinding: ActivityFragmentContainerBinding, viewModel: M) {
        super.onPrepare(viewBinding, viewModel)

        setSupportActionBar(viewBinding.toolBarFragmentContainer.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @CallSuper
    override fun onInitView(viewBinding: ActivityFragmentContainerBinding, viewModel: M) {
        super.onInitView(viewBinding, viewModel)
        iFragmentContact = WebCourseProviderFragment().apply {
            arguments = bundleOf(
                WebCourseProviderFragment.EXTRA_INIT_PAGE_URL to viewModel.initPageUrl,
                WebCourseProviderFragment.EXTRA_AUTHOR_NAME to viewModel.authorName
            )
            supportFragmentManager.commitNow {
                replace(R.id.fragmentContainer, this@apply)
            }
        }
    }

    @CallSuper
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    final override fun onShowCourseAdapterError(exception: CourseAdapterException) {
        ViewUtils.showCourseImportErrorSnackBar(this, requireViewBinding().layoutFragmentContainer, exception)
    }
}