package tool.xfy9326.schedule.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.databinding.ItemCourseTimeBinding
import tool.xfy9326.schedule.kt.getStringArray
import tool.xfy9326.schedule.ui.recyclerview.ListViewBindingAdapter
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder

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
        holder.viewBinding.textViewCourseWeekNum.text =
            holder.viewContext.getString(
                R.string.course_detail_week_num,
                element.weekNumDescription().getText(holder.viewContext)
            )
        holder.viewBinding.textViewCourseClassTime.text =
            holder.viewContext.getString(
                R.string.course_detail_class_time_simple,
                holder.viewContext.getString(R.string.weekday, weekDayStrArr[element.classTime.weekDay.ordinal]),
                element.classTime.classTimeDescription()
            )
        holder.viewBinding.textViewCourseLocation.text =
            holder.viewContext.getString(
                R.string.course_detail_location,
                element.location ?: holder.viewContext.getString(R.string.not_set)
            )
        holder.viewBinding.cardViewCourseTime.setOnClickListener {
            onCourseTimeEditListener?.invoke(holder.adapterPosition, element)
        }
        holder.viewBinding.buttonDeleteCourseTime.setOnClickListener {
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