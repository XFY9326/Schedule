package tool.xfy9326.schedule.ui.activity.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.kt.getSuperGenericTypeClass

abstract class ViewBindingActivity<V : ViewBinding> : AbstractActivity() {
    companion object {
        private const val REFLECT_METHOD_INFLATE = "inflate"
    }

    private lateinit var viewBinding: V

    protected open fun onCreateViewBinding(): V {
        val viewBindingClass = this::class.getSuperGenericTypeClass<V>(0)
        val inflateMethod = viewBindingClass.getMethod(REFLECT_METHOD_INFLATE, LayoutInflater::class.java)
        return viewBindingClass.cast(inflateMethod.invoke(null, layoutInflater))!!
    }

    protected open fun onBindView(viewBinding: V) {
        setContentView(viewBinding.root)
    }

    protected open fun onInitView(viewBinding: V) {}

    protected open fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: V) {}

    final override fun onActivityInit(savedInstanceState: Bundle?) {
        viewBinding = onCreateViewBinding()
        onBindView(viewBinding)

        onInitView(viewBinding)

        onHandleSavedInstanceState(savedInstanceState, viewBinding)
    }

    fun requireViewBinding(): V = viewBinding
}