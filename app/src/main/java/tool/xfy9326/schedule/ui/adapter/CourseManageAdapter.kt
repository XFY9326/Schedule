package tool.xfy9326.schedule.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.databinding.ItemCourseBinding
import tool.xfy9326.schedule.kt.setOnSingleClickListener
import tool.xfy9326.schedule.ui.view.recyclerview.AdvancedDividerItemDecoration
import tool.xfy9326.schedule.ui.view.recyclerview.BaseDifferItemCallBack
import tool.xfy9326.schedule.ui.view.recyclerview.SwipeListViewBindingAdapter

class CourseManageAdapter : SwipeListViewBindingAdapter<Course, ItemCourseBinding>(CourseDifferCallback()) {
    private var onCourseEditListener: ((Course) -> Unit)? = null
    private var onCourseSwipedListener: ((Course) -> Unit)? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(AdvancedDividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL))
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateContentViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemCourseBinding =
        ItemCourseBinding.inflate(layoutInflater, parent, false)

    override fun onBindViewHolder(viewContext: Context, binding: ItemCourseBinding, position: Int, element: Course) {
        binding.textViewCourseName.apply {
            isSelected = true
            text = element.name
        }
        binding.imageViewCourseColor.imageTintList = ColorStateList.valueOf(element.color)
        binding.layoutCourseItem.setOnSingleClickListener {
            onCourseEditListener?.invoke(element)
        }
    }

    override fun onItemSwiped(position: Int, element: Course) {
        onCourseSwipedListener?.invoke(element)
    }

    fun setOnCourseEditListener(onCourseEditListener: ((Course) -> Unit)?) {
        this.onCourseEditListener = onCourseEditListener
    }

    fun setOnCourseSwipedListener(onCourseSwipedListener: ((Course) -> Unit)?) {
        this.onCourseSwipedListener = onCourseSwipedListener
    }

    private class CourseDifferCallback : BaseDifferItemCallBack<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean =
            oldItem.courseId == newItem.courseId

        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean =
            oldItem.name == newItem.name && oldItem.color == newItem.color
    }
}