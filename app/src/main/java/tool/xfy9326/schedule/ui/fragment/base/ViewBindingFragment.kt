@file:Suppress("unused")

package tool.xfy9326.schedule.ui.fragment.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import lib.xfy9326.android.kit.removeSelf

abstract class ViewBindingFragment<V : ViewBinding> : Fragment() {
    private var viewBinding: V? = null

    protected abstract fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): V

    protected abstract fun onBindViewBinding(view: View): V

    protected open fun onInitView(viewBinding: V) {}

    protected open fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: V) {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (view == null) {
            onCreateViewBinding(inflater, container).apply {
                viewBinding = this
                onInitView(requireViewBinding())
            }.root
        } else {
            val oldView = requireView()
            oldView.removeSelf()
            viewBinding = onBindViewBinding(oldView)
            onInitView(requireViewBinding())
            oldView
        }
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onHandleSavedInstanceState(savedInstanceState, requireViewBinding())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    fun requireViewBinding(): V = viewBinding!!
}