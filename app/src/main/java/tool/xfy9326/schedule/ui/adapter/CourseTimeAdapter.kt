package tool.xfy9326.schedule.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.databinding.ItemCourseTimeBinding
import tool.xfy9326.schedule.kt.getStringArray
import tool.xfy9326.schedule.kt.setOnSingleClickListener
import tool.xfy9326.schedule.ui.recyclerview.ListViewBindingAdapter
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder
import tool.xfy9326.schedule.utils.schedule.WeekNumPattern

class CourseTimeAdapter : ListViewBindingAdapter<CourseTime, ItemCourseTimeBinding, ViewBindingViewHolder<ItemCourseTimeBinding>>() {
    private lateinit var weekDayStrArr: Array<String>
    private var onCourseTimeEditListener: ((Int, CourseTime) -> Unit)? = null
    private var onCourseTimeDeleteListener: ((Int, CourseTime) -> Unit)? = null

    override fun onCreateViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int) =
        ViewBindingViewHolder(ItemCourseTimeBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewBindingViewHolder<ItemCourseTimeBinding>, position: Int, element: CourseTime) {
        if (!::weekDayStrArr.isInitialized) {
            weekDayStrArr = holder.viewContext.getStringArray(R.array.weekday)
        }
        val weekNumText = WeekNumPattern(element.weekNum).getText(holder.viewContext)
        holder.viewBinding.textViewCourseWeekNum.text =
            if (weekNumText.isEmpty()) {
                holder.viewContext.getString(R.string.course_detail_week_num_simple, holder.viewContext.getString(R.string.undefined))
            } else {
                holder.viewContext.getString(R.string.course_detail_week_num, weekNumText)
            }
        holder.viewBinding.textViewCourseClassTime.text =
            holder.viewContext.getString(
                R.string.course_detail_class_time_simple,
                holder.viewContext.getString(R.string.weekday, weekDayStrArr[element.classTime.weekDay.ordinal]),
                element.classTime.classTimeDescription()
            )
        holder.viewBinding.textViewCourseLocation.text =
            holder.viewContext.getString(
                R.string.course_detail_location,
                element.location ?: holder.viewContext.getString(R.string.undefined)
            )
        holder.viewBinding.cardViewCourseTime.setOnSingleClickListener {
            onCourseTimeEditListener?.invoke(holder.adapterPosition, element)
        }
        holder.viewBinding.buttonDeleteCourseTime.setOnSingleClickListener {
            onCourseTimeDeleteListener?.invoke(holder.adapterPosition, element)
        }
    }

    fun setOnCourseTimeEditListener(onCourseTimeEditListener: ((Int, CourseTime) -> Unit)?) {
        this.onCourseTimeEditListener = onCourseTimeEditListener
    }

    fun setOnCourseTimeDeleteListener(onCourseTimeDeleteListener: ((Int, CourseTime) -> Unit)?) {
        this.onCourseTimeDeleteListener = onCourseTimeDeleteListener
    }
}