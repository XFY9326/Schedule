package tool.xfy9326.schedule.ui.adapter

import android.content.res.ColorStateList
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleWrapper
import tool.xfy9326.schedule.databinding.ItemScheduleBinding
import tool.xfy9326.schedule.kt.getColorCompat
import tool.xfy9326.schedule.kt.setOnSingleClickListener
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.recyclerview.BaseDifferItemCallBack
import tool.xfy9326.schedule.ui.recyclerview.ListViewBindingAdapter
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder

class ScheduleManageAdapter :
    ListViewBindingAdapter<ScheduleWrapper, ItemScheduleBinding, ViewBindingViewHolder<ItemScheduleBinding>>(ScheduleDifferCallback()) {
    private var onScheduleOperateListener: ((ScheduleWrapper, ScheduleOperation) -> Unit)? = null

    override fun onCreateViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int) =
        ViewBindingViewHolder(ItemScheduleBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewBindingViewHolder<ItemScheduleBinding>, position: Int, element: ScheduleWrapper) {
        holder.viewBinding.textViewScheduleName.apply {
            isSelected = true
            text = element.schedule.name
        }
        holder.viewBinding.cardViewSchedule.setCardBackgroundColor(element.schedule.color)

        if (MaterialColorHelper.isLightColor(element.schedule.color)) {
            val darkColor = holder.viewContext.getColorCompat(R.color.schedule_text_dark)
            holder.viewBinding.textViewScheduleName.setTextColor(darkColor)
            holder.viewBinding.buttonScheduleEdit.imageTintList = ColorStateList.valueOf(darkColor)
            holder.viewBinding.imageViewScheduleCurrent.imageTintList =
                ColorStateList.valueOf(holder.viewContext.getColorCompat(R.color.dark_blue_icon))
        } else {
            val lightColor = holder.viewContext.getColorCompat(R.color.schedule_text_light)
            holder.viewBinding.textViewScheduleName.setTextColor(lightColor)
            holder.viewBinding.buttonScheduleEdit.imageTintList = ColorStateList.valueOf(lightColor)
            holder.viewBinding.imageViewScheduleCurrent.imageTintList =
                ColorStateList.valueOf(holder.viewContext.getColorCompat(R.color.light_blue_icon))
        }

        holder.viewBinding.imageViewScheduleCurrent.isVisible = element.inUsing

        holder.viewBinding.cardViewSchedule.setOnLongClickListener {
            onScheduleOperateListener?.invoke(element, ScheduleOperation.SET_AS_CURRENT)
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            true
        }
        holder.viewBinding.cardViewSchedule.setOnSingleClickListener {
            onScheduleOperateListener?.invoke(element, ScheduleOperation.COURSE_EDIT)
        }
        holder.viewBinding.buttonScheduleEdit.setOnSingleClickListener {
            onScheduleOperateListener?.invoke(element, ScheduleOperation.EDIT)
        }
    }

    fun update(schedules: List<Schedule>, currentUsingScheduleId: Long) {
        submitList(schedules.map {
            ScheduleWrapper(it, it.scheduleId == currentUsingScheduleId)
        })
    }

    fun setOnScheduleOperateListener(listener: ((ScheduleWrapper, ScheduleOperation) -> Unit)?) {
        this.onScheduleOperateListener = listener
    }

    enum class ScheduleOperation {
        EDIT,
        COURSE_EDIT,
        SET_AS_CURRENT
    }

    private class ScheduleDifferCallback : BaseDifferItemCallBack<ScheduleWrapper>() {
        override fun areItemsTheSame(oldItem: ScheduleWrapper, newItem: ScheduleWrapper): Boolean =
            oldItem.schedule.scheduleId == newItem.schedule.scheduleId

        override fun areContentsTheSame(oldItem: ScheduleWrapper, newItem: ScheduleWrapper): Boolean =
            oldItem.schedule.name == newItem.schedule.name
                    && newItem.inUsing == oldItem.inUsing
                    && oldItem.schedule.color == newItem.schedule.color
    }
}