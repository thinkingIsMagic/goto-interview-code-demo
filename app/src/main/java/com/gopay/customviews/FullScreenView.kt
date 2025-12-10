package com.gopay.customviews

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.airbnb.lottie.LottieAnimationView
import com.gopay.R
import com.gopay.customviews.FullScreenViewType.ErrorView
import com.gopay.customviews.FullScreenViewType.LoadingView
import com.gopay.extensions.gone
import com.gopay.extensions.isGone
import com.gopay.extensions.isVisible
import com.gopay.extensions.visible

/**
 * 全屏视图自定义控件
 * 
 * 这是一个自定义的View，用于在全屏显示加载状态或错误状态。
 * 通常用于：
 * - 数据加载时显示加载动画
 * - 网络错误或其他错误时显示错误信息
 * 
 * 技术实现：
 * - 继承自FrameLayout，可以包含其他子视图
 * - 使用Lottie动画库显示动画效果
 * - 使用扩展函数简化视图的显示/隐藏操作
 * 
 * @param context Android上下文
 * @param attrs 属性集合（从XML布局文件中读取的属性）
 * @param defStyleAttr 默认样式属性
 */
class FullScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * 加载视图容器
     * 使用lazy延迟初始化，只有在第一次访问时才会查找视图
     */
    private val loader by lazy { findViewById<CardView>(R.id.loader) }
    
    /**
     * 错误动画视图
     * LottieAnimationView用于播放Lottie动画（JSON格式的动画文件）
     */
    private val errorView by lazy { findViewById<LottieAnimationView>(R.id.error_view) }
    
    /**
     * 错误文本视图
     * 用于显示错误信息文字
     */
    private val errorText by lazy { findViewById<TextView>(R.id.error_text) }

    /**
     * 初始化块
     * 在对象创建时执行，用于加载布局文件
     */
    init {
        // 将布局文件加载到这个View中
        // R.layout.full_screen_view 对应 res/layout/full_screen_view.xml
        inflate(context, R.layout.full_screen_view, this)
    }

    /**
     * 显示指定类型的视图
     * 
     * @param type 要显示的视图类型（LoadingView或ErrorView）
     */
    fun show(type: FullScreenViewType) {
        when (type) {
            // 显示加载视图
            LoadingView -> {
                // 如果加载视图当前是隐藏的，则显示它
                if (loader.isGone()) {
                    loader.visible()
                }
            }

            // 显示错误视图
            ErrorView -> {
                // 如果加载视图正在显示，先隐藏它
                if (loader.isVisible()) {
                    loader.gone()
                }
                // 显示错误动画
                if (errorView.isGone()) {
                    errorView.visible()
                }
                // 显示错误文本
                if (errorText.isGone()) {
                    errorText.visible()
                }
            }
        }
    }

    /**
     * 隐藏指定类型的视图
     * 
     * @param type 要隐藏的视图类型（LoadingView或ErrorView）
     */
    fun hide(type: FullScreenViewType) {
        when (type) {
            // 隐藏加载视图
            LoadingView -> {
                if (loader.isVisible()) {
                    loader.gone()
                }
            }

            // 隐藏错误视图
            ErrorView -> {
                // 隐藏错误动画
                if (errorView.isVisible()) {
                    errorView.gone()
                }
                // 隐藏错误文本
                if (errorText.isVisible()) {
                    errorText.gone()
                }
            }
        }
    }
}
