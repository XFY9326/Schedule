package tool.xfy9326.schedule.ui.base

interface OnRequestBackCallback {
    /**
     * On request back
     *
     * @return true: Handled   false: Not handled
     */
    fun onRequestBack(): Boolean
}