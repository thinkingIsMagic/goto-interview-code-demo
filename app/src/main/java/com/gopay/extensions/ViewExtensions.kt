package com.gopay.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * View扩展函数集合
 * 
 * 这些扩展函数简化了View的常用操作，使代码更简洁易读。
 * Kotlin的扩展函数允许我们为现有类添加新方法，而不需要修改原始类。
 */

/**
 * 显示视图
 * 
 * 将视图的可见性设置为VISIBLE（可见）
 * 
 * 使用示例：
 * textView.visible()
 */
fun View.visible() {
    this.visibility = View.VISIBLE
}

/**
 * 隐藏视图
 * 
 * 将视图的可见性设置为GONE（完全隐藏，不占用布局空间）
 * 
 * 注意：GONE和INVISIBLE的区别：
 * - GONE：完全隐藏，不占用空间
 * - INVISIBLE：不可见，但仍占用空间
 * 
 * 使用示例：
 * textView.gone()
 */
fun View.gone() {
    this.visibility = View.GONE
}

/**
 * 检查视图是否可见
 * 
 * @return true表示视图当前是可见的（VISIBLE）
 * 
 * 使用示例：
 * if (textView.isVisible()) { ... }
 */
fun View.isVisible() = this.visibility == View.VISIBLE

/**
 * 检查视图是否隐藏
 * 
 * @return true表示视图当前是隐藏的（GONE）
 * 
 * 使用示例：
 * if (textView.isGone()) { ... }
 */
fun View.isGone() = this.visibility == View.GONE

/**
 * 在ViewGroup中加载布局
 * 
 * 这是一个便利函数，用于在ViewGroup中加载布局文件。
 * 
 * @param layoutRes 布局资源ID（如R.layout.activity_main）
 * @param attachToRoot 是否将加载的视图附加到根视图
 *                     false：只加载视图，不附加（通常用于RecyclerView的ViewHolder）
 *                     true：加载并附加到父视图
 * @return 加载的View对象
 * 
 * 使用示例：
 * val view = parentView.inflate(R.layout.item_list)
 */
fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)

