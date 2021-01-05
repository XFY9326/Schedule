package tool.xfy9326.schedule.content.base

abstract class BaseCourseProvider {
    private var _params: Array<Any?> = emptyArray()
    protected val params: Array<Any?>
        get() = _params

    fun initParams(params: Array<Any?>) {
        this._params = params
    }
}