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
