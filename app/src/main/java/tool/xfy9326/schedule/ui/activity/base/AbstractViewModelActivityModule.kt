package tool.xfy9326.schedule.ui.activity.base

import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

abstract class AbstractViewModelActivityModule<M : AbstractViewModel, V : ViewBinding, T : ViewModelActivity<M, V>>(activity: T) : AbstractActivityModule<T>(activity) {
    protected fun requireViewModel(): M = requireActivity().requireViewModel()

    protected fun requireViewBinding(): V = requireActivity().requireViewBinding()
}