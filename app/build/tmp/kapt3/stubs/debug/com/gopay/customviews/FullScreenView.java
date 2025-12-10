package com.gopay.customviews;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B%\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010\u001e\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dR#\u0010\t\u001a\n \u000b*\u0004\u0018\u00010\n0\n8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\rR#\u0010\u0010\u001a\n \u000b*\u0004\u0018\u00010\u00110\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\u000f\u001a\u0004\b\u0012\u0010\u0013R#\u0010\u0015\u001a\n \u000b*\u0004\u0018\u00010\u00160\u00168BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0019\u0010\u000f\u001a\u0004\b\u0017\u0010\u0018\u00a8\u0006\u001f"}, d2 = {"Lcom/gopay/customviews/FullScreenView;", "Landroid/widget/FrameLayout;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "errorText", "Landroid/widget/TextView;", "kotlin.jvm.PlatformType", "getErrorText", "()Landroid/widget/TextView;", "errorText$delegate", "Lkotlin/Lazy;", "errorView", "Lcom/airbnb/lottie/LottieAnimationView;", "getErrorView", "()Lcom/airbnb/lottie/LottieAnimationView;", "errorView$delegate", "loader", "Landroidx/cardview/widget/CardView;", "getLoader", "()Landroidx/cardview/widget/CardView;", "loader$delegate", "hide", "", "type", "Lcom/gopay/customviews/FullScreenViewType;", "show", "app_debug"})
public final class FullScreenView extends android.widget.FrameLayout {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy loader$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy errorView$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy errorText$delegate = null;
    
    @kotlin.jvm.JvmOverloads()
    public FullScreenView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs, int defStyleAttr) {
        super(null);
    }
    
    private final androidx.cardview.widget.CardView getLoader() {
        return null;
    }
    
    private final com.airbnb.lottie.LottieAnimationView getErrorView() {
        return null;
    }
    
    private final android.widget.TextView getErrorText() {
        return null;
    }
    
    public final void show(@org.jetbrains.annotations.NotNull()
    com.gopay.customviews.FullScreenViewType type) {
    }
    
    public final void hide(@org.jetbrains.annotations.NotNull()
    com.gopay.customviews.FullScreenViewType type) {
    }
    
    @kotlin.jvm.JvmOverloads()
    public FullScreenView(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    @kotlin.jvm.JvmOverloads()
    public FullScreenView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
}