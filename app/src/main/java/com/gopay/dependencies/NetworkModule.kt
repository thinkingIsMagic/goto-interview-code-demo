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
 * 网络模块
 * 
 * 这个模块使用Koin依赖注入框架配置网络相关的依赖。
 * 包括：
 * - Gson：JSON序列化/反序列化库
 * - OkHttp：HTTP客户端，负责实际的网络请求
 * - Retrofit：REST API客户端，基于OkHttp构建
 * - RxJava2适配器：将Retrofit的响应转换为RxJava的Observable
 * 
 * 使用factory：每次注入时创建新实例
 * 使用single：整个应用生命周期中只有一个实例（单例）
 */
val networkModule = module {
    // Gson转换器工厂，用于将JSON字符串转换为Java/Kotlin对象
    factory<GsonConverterFactory> {
        GsonConverterFactory.create()
    }
    
    // Gson对象，用于JSON的序列化和反序列化
    factory <Gson> {
        GsonBuilder().create()
    }
    
    // RxJava2适配器工厂，用于将Retrofit的Call转换为RxJava的Observable
    factory<RxJava2CallAdapterFactory> {
        RxJava2CallAdapterFactory.create()
    }
    
    // HTTP缓存，用于缓存网络请求的响应
    factory {
        providesCache(get())
    }
    
    // OkHttp客户端，配置了缓存、超时时间和日志拦截器
    factory {
        providesOkhttp(get())
    }
    
    // Retrofit实例，这是网络请求的核心，整个应用只需要一个实例
    single {
        providesRetrofit(get(), get(), get())
    }
}

/**
 * 提供HTTP缓存
 * 
 * @param context Android上下文，用于获取缓存目录
 * @return Cache对象，用于OkHttp的响应缓存
 */
private fun providesCache(context: Context) : Cache {
    // 设置缓存大小为10MB
    val cacheSize = 10 * 1024 * 1024 // 10 MB
    // 在应用的缓存目录中创建缓存
    return Cache(context.cacheDir, cacheSize.toLong())
}

/**
 * 提供OkHttp客户端
 * 
 * 配置了：
 * - 缓存：使用上面创建的Cache对象
 * - 连接超时：10秒
 * - 写入超时：30秒（上传数据时）
 * - 读取超时：10秒（下载数据时）
 * - 日志拦截器：仅在DEBUG模式下启用，用于查看网络请求和响应的详细信息
 * 
 * @param cache HTTP缓存对象
 * @return 配置好的OkHttpClient实例
 */
private fun providesOkhttp(cache: Cache) : OkHttpClient {
    val client = OkHttpClient.Builder()
        .cache(cache)  // 启用HTTP缓存
        .connectTimeout(10, TimeUnit.SECONDS)  // 连接超时时间
        .writeTimeout(30, TimeUnit.SECONDS)    // 写入超时时间（上传）
        .readTimeout(10, TimeUnit.SECONDS)     // 读取超时时间（下载）

    // 仅在DEBUG模式下添加日志拦截器
    // 这样可以查看网络请求的详细信息，方便调试
    if (BuildConfig.DEBUG) {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY  // 记录请求和响应的完整内容
        client.addInterceptor(logging)
    }

    return client.build()
}

/**
 * 提供Retrofit实例
 * 
 * Retrofit是一个类型安全的HTTP客户端，用于Android和Java。
 * 它通过接口定义API端点，然后自动生成实现。
 * 
 * 配置：
 * - baseUrl：API的基础URL（从BuildConfig中读取）
 * - converterFactory：Gson转换器，用于将JSON转换为对象
 * - callAdapterFactory：RxJava2适配器，用于将Call转换为Observable
 * - client：使用上面配置的OkHttpClient
 * 
 * @param gsonConverterFactory Gson转换器工厂
 * @param rxJava2CallAdapterFactory RxJava2适配器工厂
 * @param okHttpClient OkHttp客户端
 * @return 配置好的Retrofit实例
 */
private fun providesRetrofit(
    gsonConverterFactory: GsonConverterFactory,
    rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
    okHttpClient: OkHttpClient
): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE)  // API的基础URL
        .addConverterFactory(gsonConverterFactory)  // 添加Gson转换器
        .addCallAdapterFactory(rxJava2CallAdapterFactory)  // 添加RxJava2适配器
        .client(okHttpClient)  // 使用配置好的OkHttpClient
        .build()
}
