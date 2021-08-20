package tool.xfy9326.schedule.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import lib.xfy9326.android.kit.getDrawableCompat
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.CourseTime.Companion.getWeeksDescriptionText
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.SectionTime.Companion.description
import tool.xfy9326.schedule.beans.SectionTime.Companion.scheduleTimeDescription
import tool.xfy9326.schedule.databinding.ItemCourseDetailTimeBinding
import tool.xfy9326.schedule.databinding.ItemCourseDetailTimeExpandBinding
import tool.xfy9326.schedule.ui.view.recyclerview.AdvancedDividerItemDecoration
import tool.xfy9326.schedule.ui.view.recyclerview.BaseViewBindingAdapter
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder

class CourseDetailAdapter(
    courseTimes: List<CourseTime>,
    timeId: Long,
    private var scheduleTimes: List<ScheduleTime>,
    private val weekDayStrArray: Array<String>,
    initShowMore: Boolean,
) : BaseViewBindingAdapter<ViewBinding, ViewBindingViewHolder<ViewBinding>>() {
    companion object {
        private const val VIEW_TYPE_COURSE_TIME = 1
        private const val VIEW_TYPE_EXPAND_BUTTON = 2
    }

    private val sortedCourseTimes = courseTimes.toMutableList().also {
        if (it.size > 1) {
            val cellCourseTime = courseTimes.find { time ->
                time.timeId == timeId
            }
            if (cellCourseTime != null) {
                it.remove(cellCourseTime)
                it.add(0, cellCourseTime)
            }
        }
    }
    val timesSize = sortedCourseTimes.size
    private var showSize =
        if (initShowMore) {
            timesSize
        } else {
            1
        }
    var isExpanded = initShowMore
        private set

    private fun showMore() {
        if (timesSize > 1) {
            showSize = timesSize
            notifyItemRangeInserted(1, timesSize - 1)
        }
    }

    private fun showLess() {
        if (timesSize > 1) {
            showSize = 1
            notifyItemRangeRemoved(1, timesSize - 1)
        }
    }

    override fun getItemCount(): Int = showSize + if (timesSize > 1) 1 else 0

    override fun getItemViewType(position: Int) =
        if (timesSize > 1 && position == itemCount - 1) {
            VIEW_TYPE_EXPAND_BUTTON
        } else {
            VIEW_TYPE_COURSE_TIME
        }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        val dividerMargin = recyclerView.context.resources.getDimensionPixelSize(R.dimen.divider_margin_course_detail_time)

        recyclerView.addItemDecoration(
            AdvancedDividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL, dividerMargin).also {
                it.setDividerDrawable(recyclerView.context.getDrawableCompat(R.color.dark_gray_icon)!!)
                it.setOnDrawDividerListener { position, itemAmount ->
                    if (timesSize > 1) {
                        position < itemAmount - 2
                    } else {
                        position != itemAmount - 1
                    }
                }
            }
        )
    }

    override fun onCreateViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int) =
        ViewBindingViewHolder(
            when (viewType) {
                VIEW_TYPE_COURSE_TIME -> ItemCourseDetailTimeBinding.inflate(layoutInflater, parent, false)
                VIEW_TYPE_EXPAND_BUTTON -> ItemCourseDetailTimeExpandBinding.inflate(layoutInflater, parent, false)
                else -> error("Unsupported view type!")
            }
        )

    override fun onBindViewHolder(holder: ViewBindingViewHolder<ViewBinding>, position: Int) {
        if (holder.viewBinding is ItemCourseDetailTimeBinding) {
            val courseTime = sortedCourseTimes[position]
            val weekNumText = courseTime.getWeeksDescriptionText(holder.viewContext)
            holder.viewBinding.textViewCourseWeekNum.text =
                if (weekNumText.isEmpty()) {
                    holder.viewContext.getString(R.string.course_detail_week_num_simple, holder.viewContext.getString(R.string.undefined))
                } else {
                    holder.viewContext.getString(R.string.course_detail_week_num, weekNumText)
                }
            holder.viewBinding.textViewCourseSectionTime.text = holder.viewContext.getString(
                R.string.course_detail_section_time,
                holder.viewContext.getString(R.string.weekday, weekDayStrArray[courseTime.sectionTime.weekDay.ordinal]),
                courseTime.sectionTime.description,
                courseTime.sectionTime.scheduleTimeDescription(scheduleTimes)
            )
            val location = courseTime.location
            if (location == null) {
                holder.viewBinding.textViewCourseLocation.isVisible = false
            } else {
                holder.viewBinding.textViewCourseLocation.text = holder.viewContext.getString(R.string.course_detail_location, courseTime.location)
            }
        } else if (holder.viewBinding is ItemCourseDetailTimeExpandBinding) {
            holder.viewBinding.buttonExpand.setImageResource(if (isExpanded) R.drawable.ic_expand_less_30 else R.drawable.ic_expand_more_30)
            holder.viewBinding.buttonExpand.setOnClickListener {
                if (isExpanded) {
                    (it as AppCompatImageButton).setImageResource(R.drawable.ic_expand_more_30)
                    isExpanded = false
                    showLess()
                } else {
                    (it as AppCompatImageButton).setImageResource(R.drawable.ic_expand_less_30)
                    isExpanded = true
                    showMore()
                }
            }
        }
    }
}