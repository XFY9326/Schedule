package tool.xfy9326.schedule.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig
import tool.xfy9326.schedule.databinding.ItemCourseImportBinding
import tool.xfy9326.schedule.ui.recyclerview.BaseViewBindingAdapter
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder

class CourseImportAdapter : BaseViewBindingAdapter<ItemCourseImportBinding, ViewBindingViewHolder<ItemCourseImportBinding>>() {
    private var sortedConfigs = ArrayList<AbstractCourseImportConfig<*, *, *, *>>()
    private var onCourseImportItemClickListener: ((AbstractCourseImportConfig<*, *, *, *>) -> Unit)? = null

    fun updateConfigs(configs: List<AbstractCourseImportConfig<*, *, *, *>>) {
        sortedConfigs.clear()
        sortedConfigs.addAll(configs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int) =
        ViewBindingViewHolder(ItemCourseImportBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewBindingViewHolder<ItemCourseImportBinding>, position: Int) {
        holder.viewBinding.textViewCourseImportSchoolName.apply {
            isSelected = true
            text = holder.viewContext.getString(sortedConfigs[position].schoolNameResId)
        }
        holder.viewBinding.textViewCourseImportSystemName.apply {
            isSelected = true
            text = holder.viewContext.getString(sortedConfigs[position].systemNameResId)
        }
        holder.viewBinding.layoutCourseImportItem.setOnClickListener {
            onCourseImportItemClickListener?.invoke(sortedConfigs[holder.adapterPosition])
        }
    }

    fun setOnCourseImportItemClickListener(onCourseImportItemClickListener: ((AbstractCourseImportConfig<*, *, *, *>) -> Unit)?) {
        this.onCourseImportItemClickListener = onCourseImportItemClickListener
    }

    override fun getItemCount(): Int = sortedConfigs.size
}