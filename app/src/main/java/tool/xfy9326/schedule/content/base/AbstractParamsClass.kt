@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.base

abstract class AbstractParamsClass<P> {
    private var _params: P? = null
    protected val params
        get() = _params

    fun setParams(params: P?) {
        _params = params
    }

    fun requireParams() = params!!
}