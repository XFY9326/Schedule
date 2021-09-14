package tool.xfy9326.schedule.ui.view.schedule

interface IScheduleCell {
    fun getColumn(): Int

    fun getRow(): Int

    fun getRowSpan(): Int
}