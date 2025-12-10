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
 * 全屏加载/错误视图容器。
 * - LoadingView：显示居中的卡片式加载动画
 * - ErrorView：显示 Lottie 错误动画和提示文案
 *
 * 通过 show/hide 方法控制可见性，方便在列表或整页请求时复用。
 */
class FullScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val loader by lazy { findViewById<CardView>(R.id.loader) }
    private val errorView by lazy { findViewById<LottieAnimationView>(R.id.error_view) }
    private val errorText by lazy { findViewById<TextView>(R.id.error_text) }

    init {
        inflate(context, R.layout.full_screen_view, this)
    }

    /**
     * 根据类型展示对应视图。若当前已可见则避免重复操作。
     */
    fun show(type: FullScreenViewType) {
        when (type) {
            LoadingView -> {
                if (loader.isGone()) {
                    loader.visible()
                }
            }

            ErrorView -> {
                if (loader.isVisible()) {
                    loader.gone()
                }
                if (errorView.isGone()) {
                    errorView.visible()
                }
                if (errorText.isGone()) {
                    errorText.visible()
                }
            }
        }
    }

    /**
     * 隐藏指定类型的视图，避免与业务内容重叠。
     */
    fun hide(type: FullScreenViewType) {
        when (type) {
            LoadingView -> {
                if (loader.isVisible()) {
                    loader.gone()
                }
            }

            ErrorView -> {
                if (errorView.isVisible()) {
                    errorView.gone()
                }
                if (errorText.isVisible()) {
                    errorText.gone()
                }
            }
        }
    }
}
