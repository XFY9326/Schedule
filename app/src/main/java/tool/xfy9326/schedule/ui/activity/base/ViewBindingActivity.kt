package tool.xfy9326.schedule.ui.activity.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class ViewBindingActivity<V : ViewBinding> : AbstractActivity() {
    private lateinit var viewBinding: V

    protected abstract fun onCreateViewBinding(): V

    protected open fun onBindView(viewBinding: V) {
        setContentView(viewBinding.root)
    }

    protected open fun onInitView(viewBinding: V) {}

    protected open fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: V) {}

    protected open fun onContentViewPreload(savedInstanceState: Bundle?) {}

    final override fun onActivityInit(savedInstanceState: Bundle?) {
        onContentViewPreload(savedInstanceState)

        viewBinding = onCreateViewBinding()
        onBindView(viewBinding)

        onInitView(viewBinding)

        onHandleSavedInstanceState(savedInstanceState, viewBinding)
    }

    fun requireViewBinding(): V = viewBinding
}