package tool.xfy9326.schedule.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.endTimeStr
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.startTimeStr
import tool.xfy9326.schedule.databinding.ItemScheduleTimeBinding
import tool.xfy9326.schedule.ui.view.recyclerview.ListViewBindingAdapter
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder

class ScheduleTimeAdapter : ListViewBindingAdapter<ScheduleTime, ItemScheduleTimeBinding, ViewBindingViewHolder<ItemScheduleTimeBinding>>() {
    private var onScheduleTimeEditListener: ((Int, Int, Int, Boolean) -> Unit)? = null

    override fun onCreateViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int) =
        ViewBindingViewHolder(ItemScheduleTimeBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewBindingViewHolder<ItemScheduleTimeBinding>, position: Int, element: ScheduleTime) {
        holder.viewBinding.apply {
            textViewScheduleTimeIndex.apply {
                isSelected = true
                text = holder.viewContext.getString(R.string.course_list_num, position + 1)
            }
            textViewScheduleTimeStart.text = element.startTimeStr
            textViewScheduleTimeEnd.text = element.endTimeStr
            textViewScheduleTimeStart.setOnSingleClickListener {
                onScheduleTimeEditListener?.invoke(holder.bindingAdapterPosition, element.startHour, element.startMinute, true)
            }
            textViewScheduleTimeEnd.setOnSingleClickListener {
                onScheduleTimeEditListener?.invoke(holder.bindingAdapterPosition, element.endHour, element.endMinute, false)
            }
        }
    }

    fun setOnScheduleTimeEditListener(listener: ((Int, Int, Int, Boolean) -> Unit)?) {
        this.onScheduleTimeEditListener = listener
    }
}