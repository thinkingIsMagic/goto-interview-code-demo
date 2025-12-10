package com.gopay

import android.app.Application
import com.gopay.dependencies.appModule
import com.gopay.dependencies.dispatcherModule
import com.gopay.dependencies.imageModule
import com.gopay.dependencies.networkModule
import com.gopay.dependencies.storageModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * 应用程序主类
 * 
 * 这个类继承自Application，是Android应用的入口点。
 * 主要负责在应用启动时初始化依赖注入框架Koin。
 * 
 * 功能：
 * 1. 在onCreate()方法中初始化Koin依赖注入容器
 * 2. 注册所有需要的依赖模块（网络、存储、图片加载、协程调度器等）
 * 3. 配置Android上下文和日志记录器
 */
class App : Application() {

    /**
     * 应用创建时的回调方法
     * 
     * 当应用启动时，系统会调用这个方法。
     * 在这里我们初始化Koin依赖注入框架，注册所有需要的模块。
     */
    override fun onCreate() {
        super.onCreate()
        
        // 启动Koin依赖注入框架
        startKoin {
            // 启用Android日志记录器，方便调试时查看依赖注入的日志
            androidLogger()
            
            // 设置Android上下文，这样Koin可以在需要时注入Context
            androidContext(this@App)
            
            // 注册所有依赖模块
            // 这些模块定义了应用中需要的各种依赖（网络、数据库、图片加载器等）
            modules(
                listOf(
                    appModule,          // 应用模块（当前为空，可扩展）
                    dispatcherModule,   // 协程调度器模块
                    imageModule,        // 图片加载模块（Picasso）
                    networkModule,      // 网络请求模块（Retrofit + OkHttp）
                    storageModule       // 存储模块（Room数据库 + SharedPreferences）
                )
            )
        }
    }
}
