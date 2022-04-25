package tool.xfy9326.schedule.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.content.base.ICourseImportConfig
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.utils.BaseCourseImportConfig
import tool.xfy9326.schedule.databinding.ItemCourseImportBinding
import tool.xfy9326.schedule.databinding.LayoutSwipeItemBinding
import tool.xfy9326.schedule.ui.view.recyclerview.ListViewBindingAdapter
import tool.xfy9326.schedule.ui.view.recyclerview.SwipeItemCallback
import tool.xfy9326.schedule.ui.view.recyclerview.SwipeItemViewHolder
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder

class CourseImportAdapter : ListViewBindingAdapter<ICourseImportConfig, ViewBinding, ViewBindingViewHolder<out ViewBinding>>() {
    private var onCourseImportItemListener: OnCourseImportItemListener? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        ItemTouchHelper(SwipeItemCallback().apply {
            setOnItemSwipedListener(::onSwipe)
        }).attachToRecyclerView(recyclerView)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is BaseCourseImportConfig -> 0
            is JSConfig -> 1
            else -> error("Invalid element type! Class: ${getItem(position).javaClass}")
        }
    }

    override fun onCreateViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBindingViewHolder<out ViewBinding> {
        return when (viewType) {
            0 -> ViewBindingViewHolder(ItemCourseImportBinding.inflate(layoutInflater, parent, false))
            1 -> LayoutSwipeItemBinding.inflate(layoutInflater, parent, false).run {
                SwipeItemViewHolder(ItemCourseImportBinding.inflate(layoutInflater, layoutSwipeForeground, true), this)
            }
            else -> error("Invalid element type! ViewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewBindingViewHolder<out ViewBinding>, position: Int, element: ICourseImportConfig) {
        val viewBinding = if (holder is SwipeItemViewHolder<*> && holder.binding is ItemCourseImportBinding) {
            holder.binding
        } else if (holder.viewBinding is ItemCourseImportBinding) {
            holder.viewBinding
        } else {
            error("Invalid view holder! Class: ${holder.javaClass}")
        }
        viewBinding.textViewCourseImportSchoolName.apply {
            isSelected = true
            text = element.schoolName
        }
        viewBinding.textViewCourseImportSystemName.apply {
            isSelected = true
            text = element.systemName
        }
        viewBinding.textViewCourseImportJSConfig.isVisible = element is JSConfig
        viewBinding.layoutCourseImportItem.setOnSingleClickListener {
            if (element is BaseCourseImportConfig) {
                onCourseImportItemListener?.onCourseImportConfigClick(element)
            } else if (element is JSConfig) {
                onCourseImportItemListener?.onJSConfigClick(element)
            }
        }
    }

    fun setOnCourseImportItemListener(onCourseImportItemListener: OnCourseImportItemListener?) {
        this.onCourseImportItemListener = onCourseImportItemListener
    }

    private fun onSwipe(position: Int) {
        val element = getItem(position)
        if (element is JSConfig) {
            onCourseImportItemListener?.onJSConfigDelete(element)
        }
    }

    interface OnCourseImportItemListener {
        fun onCourseImportConfigClick(config: BaseCourseImportConfig)

        fun onJSConfigClick(jsConfig: JSConfig)

        fun onJSConfigDelete(jsConfig: JSConfig)
    }
}