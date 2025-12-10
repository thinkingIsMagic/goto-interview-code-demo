package com.gopay.dependencies;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0002\u001a\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0005H\u0002\u001a \u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\tH\u0002\"\u0011\u0010\u0000\u001a\u00020\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0012"}, d2 = {"networkModule", "Lorg/koin/core/module/Module;", "getNetworkModule", "()Lorg/koin/core/module/Module;", "providesCache", "Lokhttp3/Cache;", "context", "Landroid/content/Context;", "providesOkhttp", "Lokhttp3/OkHttpClient;", "cache", "providesRetrofit", "Lretrofit2/Retrofit;", "gsonConverterFactory", "Lretrofit2/converter/gson/GsonConverterFactory;", "rxJava2CallAdapterFactory", "Lretrofit2/adapter/rxjava2/RxJava2CallAdapterFactory;", "okHttpClient", "app_debug"})
public final class NetworkModuleKt {
    @org.jetbrains.annotations.NotNull()
    private static final org.koin.core.module.Module networkModule = null;
    
    @org.jetbrains.annotations.NotNull()
    public static final org.koin.core.module.Module getNetworkModule() {
        return null;
    }
    
    private static final okhttp3.Cache providesCache(android.content.Context context) {
        return null;
    }
    
    private static final okhttp3.OkHttpClient providesOkhttp(okhttp3.Cache cache) {
        return null;
    }
    
    private static final retrofit2.Retrofit providesRetrofit(retrofit2.converter.gson.GsonConverterFactory gsonConverterFactory, retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory rxJava2CallAdapterFactory, okhttp3.OkHttpClient okHttpClient) {
        return null;
    }
}