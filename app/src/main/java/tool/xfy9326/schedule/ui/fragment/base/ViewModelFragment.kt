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
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import kotlin.reflect.KClass

abstract class ViewModelFragment<M : AbstractViewModel, V : ViewBinding> : Fragment() {
    protected abstract val vmClass: KClass<M>
    private lateinit var viewModel: M
    private var viewBinding: V? = null

    protected open fun onGetViewModelStoreOwner(): ViewModelStoreOwner = this

    protected open fun onCreateViewModel(owner: ViewModelStoreOwner, vmClass: KClass<M>): M = ViewModelProvider(owner)[vmClass.java]

    protected abstract fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): V

    protected abstract fun onBindViewBinding(view: View): V

    protected open fun beforeBindLiveData(viewBinding: V, viewModel: M) {}

    protected open fun onBindLiveData(viewBinding: V, viewModel: M) {}

    protected open fun onInitView(viewBinding: V, viewModel: M) {}

    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = onCreateViewModel(onGetViewModelStoreOwner(), vmClass)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (view == null) {
            onCreateViewBinding(inflater, container).apply {
                viewBinding = this
            }.root
        } else {
            val oldView = requireView()
            (oldView.parent as? ViewGroup?)?.removeView(oldView)
            viewBinding = onBindViewBinding(oldView)
            oldView
        }
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        beforeBindLiveData(requireViewBinding(), requireViewModel())
        onBindLiveData(requireViewBinding(), requireViewModel())
        onInitView(requireViewBinding(), requireViewModel())

        requireViewModel().onViewInitialized(savedInstanceState == null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    fun requireViewModel(): M = viewModel

    fun requireViewBinding(): V = viewBinding!!
}