package tool.xfy9326.schedule.ui.activity.base

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import kotlin.reflect.KClass

abstract class ViewModelActivity<M : AbstractViewModel, V : ViewBinding> : AbstractActivity() {
    protected abstract val vmClass: KClass<M>

    private lateinit var viewModel: M
    private lateinit var viewBinding: V

    protected open fun onContentViewPreload(savedInstanceState: Bundle?, viewModel: M) {}

    protected open fun onGetViewModelStoreOwner(): ViewModelStoreOwner = this

    protected open fun onCreateViewModel(owner: ViewModelStoreOwner, vmClass: KClass<M>): M = ViewModelProvider(owner)[vmClass.java]

    protected abstract fun onCreateViewBinding(): V

    protected open fun onBindView(viewBinding: V) {
        setContentView(viewBinding.root)
    }

    protected open fun onPrepare(viewBinding: V, viewModel: M) {}

    protected open fun onBindLiveData(viewBinding: V, viewModel: M) {}

    protected open fun onInitView(viewBinding: V, viewModel: M) {}

    protected open fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: V, viewModel: M) {}

    final override fun onActivityInit(savedInstanceState: Bundle?) {
        viewModel = onCreateViewModel(onGetViewModelStoreOwner(), vmClass)

        onContentViewPreload(savedInstanceState, viewModel)

        viewBinding = onCreateViewBinding()
        onBindView(viewBinding)

        onPrepare(viewBinding, viewModel)
        onBindLiveData(viewBinding, viewModel)
        onInitView(viewBinding, viewModel)

        onHandleSavedInstanceState(savedInstanceState, viewBinding, viewModel)

        viewModel.initViewModel(savedInstanceState == null)
    }

    fun requireViewModel(): M = viewModel

    fun requireViewBinding(): V = viewBinding
}