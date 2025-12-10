package com.gopay.dispatcher;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000e\b\u0016\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002R\u001b\u0010\u0003\u001a\u00020\u00048VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006R\u001b\u0010\t\u001a\u00020\u00048VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\b\u001a\u0004\b\n\u0010\u0006R\u001b\u0010\f\u001a\u00020\u00048VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\b\u001a\u0004\b\r\u0010\u0006R\u001b\u0010\u000f\u001a\u00020\u00048VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0011\u0010\b\u001a\u0004\b\u0010\u0010\u0006\u00a8\u0006\u0012"}, d2 = {"Lcom/gopay/dispatcher/RealCoroutineDispatcherProvider;", "Lcom/gopay/dispatcher/CoroutineDispatcherProvider;", "()V", "default", "Lkotlinx/coroutines/CoroutineDispatcher;", "getDefault", "()Lkotlinx/coroutines/CoroutineDispatcher;", "default$delegate", "Lkotlin/Lazy;", "io", "getIo", "io$delegate", "main", "getMain", "main$delegate", "unconfirmed", "getUnconfirmed", "unconfirmed$delegate", "app_debug"})
public class RealCoroutineDispatcherProvider implements com.gopay.dispatcher.CoroutineDispatcherProvider {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy main$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy io$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy default$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy unconfirmed$delegate = null;
    
    public RealCoroutineDispatcherProvider() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.CoroutineDispatcher getMain() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.CoroutineDispatcher getIo() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.CoroutineDispatcher getDefault() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.CoroutineDispatcher getUnconfirmed() {
        return null;
    }
}