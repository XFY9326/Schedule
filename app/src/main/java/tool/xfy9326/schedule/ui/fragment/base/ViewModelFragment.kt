@file:Suppress("unused")

package tool.xfy9326.schedule.ui.fragment.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.kt.getSuperGenericTypeClass
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

abstract class ViewModelFragment<M : AbstractViewModel, V : ViewBinding> : Fragment() {
    companion object {
        private const val REFLECT_METHOD_INFLATE = "inflate"
    }

    private lateinit var viewModel: M
    private lateinit var viewBinding: V

    protected open fun onGetViewModelStoreOwner(): ViewModelStoreOwner = this

    protected open fun onCreateViewModel(owner: ViewModelStoreOwner): M = ViewModelProvider(owner)[this::class.getSuperGenericTypeClass(0)]

    protected open fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): V {
        val viewBindingClass = this::class.getSuperGenericTypeClass<V>(1)
        val inflateMethod = viewBindingClass.getMethod(REFLECT_METHOD_INFLATE, LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        return viewBindingClass.cast(inflateMethod.invoke(null, inflater, container, false))!!
    }

    protected open fun beforeBindLiveData(viewBinding: V, viewModel: M) {}

    protected open fun onBindLiveData(viewBinding: V, viewModel: M) {}

    protected open fun onInitView(viewBinding: V, viewModel: M) {}

    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = onCreateViewModel(onGetViewModelStoreOwner())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (view == null) {
            viewBinding = onCreateViewBinding(inflater, container)
            viewBinding.root
        } else {
            (requireView().parent as ViewGroup?)?.removeView(view)
            view
        }
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        beforeBindLiveData(viewBinding, viewModel)
        onBindLiveData(viewBinding, viewModel)
        onInitView(viewBinding, viewModel)

        viewModel.onViewInitialized(savedInstanceState == null)
    }

    fun requireViewModel(): M = viewModel

    fun requireViewBinding(): V = viewBinding
}