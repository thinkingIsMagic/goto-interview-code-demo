package com.gopay.customviews

/**
 * 标记全屏组件的展示形态，便于 show/hide 时使用 when 语句。
 */
sealed class FullScreenViewType {
    object LoadingView: FullScreenViewType()
    object ErrorView: FullScreenViewType()
}