package tool.xfy9326.schedule.ui.activity.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.kt.getSuperGenericTypeClass
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

abstract class ViewModelActivity<M : AbstractViewModel, V : ViewBinding> : AbstractActivity() {
    companion object {
        private const val REFLECT_METHOD_INFLATE = "inflate"
    }

    private lateinit var viewModel: M
    private lateinit var viewBinding: V

    protected open fun onGetViewModelStoreOwner(): ViewModelStoreOwner = this

    protected open fun onCreateViewModel(owner: ViewModelStoreOwner): M = ViewModelProvider(owner)[this::class.getSuperGenericTypeClass(0)]

    protected open fun onCreateViewBinding(): V {
        val viewBindingClass = this::class.getSuperGenericTypeClass<V>(1)
        val inflateMethod = viewBindingClass.getMethod(REFLECT_METHOD_INFLATE, LayoutInflater::class.java)
        return viewBindingClass.cast(inflateMethod.invoke(null, layoutInflater))!!
    }

    protected open fun onBindView(viewBinding: V) {
        setContentView(viewBinding.root)
    }

    protected open fun onPrepare(viewBinding: V, viewModel: M) {}

    protected open fun onBindLiveData(viewBinding: V, viewModel: M) {}

    protected open fun onInitView(viewBinding: V, viewModel: M) {}

    protected open fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: V, viewModel: M) {}

    final override fun onActivityInit(savedInstanceState: Bundle?) {
        viewModel = onCreateViewModel(onGetViewModelStoreOwner())

        viewBinding = onCreateViewBinding()
        onBindView(viewBinding)

        onPrepare(viewBinding, viewModel)
        onBindLiveData(viewBinding, viewModel)
        onInitView(viewBinding, viewModel)

        onHandleSavedInstanceState(savedInstanceState, viewBinding, viewModel)

        viewModel.onViewInitialized(savedInstanceState == null)
    }

    fun requireViewModel(): M = viewModel

    fun requireViewBinding(): V = viewBinding
}