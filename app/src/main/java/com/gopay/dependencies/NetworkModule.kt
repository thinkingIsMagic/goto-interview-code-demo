package com.gopay.dependencies

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.gopay.BuildConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络层依赖模块，集中提供 Retrofit、OkHttp、Gson 等组件。
 * 分层使用 factory/single，便于按需替换或在测试中 mock。
 */
val networkModule = module {
    factory<GsonConverterFactory> {
        GsonConverterFactory.create()
    }
    factory <Gson> {
        GsonBuilder().create()
    }
    factory<RxJava2CallAdapterFactory> {
        RxJava2CallAdapterFactory.create()
    }
    factory {
        providesCache(get())
    }
    factory {
        providesOkhttp(get())
    }
    single {
        providesRetrofit(get(), get(), get())
    }
}

// 统一配置缓存大小，减少网络抖动对体验的影响
private fun providesCache(context: Context) : Cache {
    val cacheSize = 10 * 1024 * 1024 // 10 MB
    return Cache(context.cacheDir, cacheSize.toLong())
}

// OkHttp 配置：超时、缓存、日志。DEBUG 下打开全量日志便于调试。
private fun providesOkhttp(cache: Cache) : OkHttpClient {
    val client = OkHttpClient.Builder()
        .cache(cache)
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)

    if (BuildConfig.DEBUG) {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        client.addInterceptor(logging)
    }

    return client.build()
}

// 构建 Retrofit，注入 converter 与 call adapter，基地址来自 BuildConfig。
private fun providesRetrofit(
    gsonConverterFactory: GsonConverterFactory,
    rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
    okHttpClient: OkHttpClient
): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE)
        .addConverterFactory(gsonConverterFactory)
        .addCallAdapterFactory(rxJava2CallAdapterFactory)
        .client(okHttpClient)
        .build()
}
