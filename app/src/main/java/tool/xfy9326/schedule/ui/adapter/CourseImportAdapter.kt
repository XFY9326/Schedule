package tool.xfy9326.schedule.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import tool.xfy9326.schedule.content.base.CourseImportConfig
import tool.xfy9326.schedule.databinding.ItemCourseImportBinding
import tool.xfy9326.schedule.ui.recyclerview.BaseViewBindingAdapter
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder

class CourseImportAdapter : BaseViewBindingAdapter<ItemCourseImportBinding, ViewBindingViewHolder<ItemCourseImportBinding>>() {
    private var importConfigs: List<CourseImportConfig<*, *>> = emptyList()
    private var onCourseImportItemClickListener: ((CourseImportConfig<*, *>) -> Unit)? = null

    override fun onCreateViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int) =
        ViewBindingViewHolder(ItemCourseImportBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewBindingViewHolder<ItemCourseImportBinding>, position: Int) {
        holder.viewBinding.textViewCourseImportSchoolName.apply {
            isSelected = true
            text = importConfigs[position].getSchoolNameText()
        }
        holder.viewBinding.textViewCourseImportSystemName.apply {
            isSelected = true
            text = importConfigs[position].getSystemNameText()
        }
        holder.viewBinding.layoutCourseImportItem.setOnClickListener {
            onCourseImportItemClickListener?.invoke(importConfigs[holder.adapterPosition])
        }
    }

    fun setOnCourseImportItemClickListener(onCourseImportItemClickListener: ((CourseImportConfig<*, *>) -> Unit)?) {
        this.onCourseImportItemClickListener = onCourseImportItemClickListener
    }

    fun updateList(importConfigs: List<CourseImportConfig<*, *>>) {
        this.importConfigs = importConfigs
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = importConfigs.size
}