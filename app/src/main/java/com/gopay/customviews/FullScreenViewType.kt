package com.gopay.customviews

/**
 * 全屏视图类型
 * 
 * 这是一个密封类（sealed class），用于表示FullScreenView可以显示的不同状态。
 * 
 * 密封类的特点：
 * - 所有子类必须在同一个文件中定义
 * - 类似于枚举，但更灵活（可以携带数据）
 * - 在when表达式中，如果处理了所有情况，不需要else分支
 * 
 * 使用场景：
 * - 加载状态：显示加载动画
 * - 错误状态：显示错误信息和动画
 * 
 * 为什么使用object而不是class？
 * - 这些类型不需要携带额外的数据
 * - 只需要区分不同的类型
 * - object是单例，更节省内存
 */
sealed class FullScreenViewType {
    /** 加载视图：显示加载动画 */
    object LoadingView: FullScreenViewType()
    
    /** 错误视图：显示错误信息和动画 */
    object ErrorView: FullScreenViewType()
}