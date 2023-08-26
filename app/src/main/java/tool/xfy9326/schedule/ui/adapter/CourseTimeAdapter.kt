package tool.xfy9326.schedule.ui.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.xfy9326.atools.ui.getColorCompat
import io.github.xfy9326.atools.ui.getStringArray
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.CourseTime.Companion.getWeeksDescriptionText
import tool.xfy9326.schedule.beans.SectionTime.Companion.description
import tool.xfy9326.schedule.databinding.ItemCourseTimeBinding
import tool.xfy9326.schedule.ui.view.recyclerview.ListViewBindingAdapter
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
        val weekNumText = element.getWeeksDescriptionText(holder.viewContext)
        holder.viewBinding.textViewCourseWeekNum.apply {
            text = if (weekNumText.isEmpty()) {
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(holder.viewContext.getColorCompat(R.color.red))
                holder.viewContext.getString(R.string.course_detail_week_num_simple, holder.viewContext.getString(R.string.undefined))
            } else {
                typeface = Typeface.DEFAULT
                setTextColor(holder.viewContext.getColorCompat(R.color.theme_color_secondary_text))
                holder.viewContext.getString(R.string.course_detail_week_num, weekNumText)
            }
        }
        holder.viewBinding.textViewCourseSectionTime.text =
            holder.viewContext.getString(
                R.string.course_detail_section_time_simple,
                holder.viewContext.getString(R.string.weekday, weekDayStrArr[element.sectionTime.weekDay.ordinal]),
                element.sectionTime.description
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