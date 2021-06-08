package tool.xfy9326.schedule.ui.view.recyclerview

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.databinding.LayoutSwipeItemBinding
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder

class SwipeItemViewHolder<V : ViewBinding>(val binding: V, contentBinding: LayoutSwipeItemBinding) :
    ViewBindingViewHolder<LayoutSwipeItemBinding>(contentBinding) {

    val foregroundSwipeView: ViewGroup = viewBinding.layoutSwipeForeground
    val backgroundSwipeView: ViewGroup = viewBinding.layoutSwipeBackground
    val swipeIconView: AppCompatImageView = viewBinding.imageViewSwipeIcon
}