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
 * 自定义 Application，负责在应用进程启动时完成全局依赖注入初始化。
 * Koin 会在此处注册各功能模块（网络、图片、存储、协程调度器等），
 * 保证 Activity / ViewModel 等组件随取随用。
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化 Koin，并将当前 Application 作为上下文传入
        startKoin {
            androidLogger()
            androidContext(this@App)
            // 注册所有模块，模块可按功能拆分方便面试时扩展
            modules(
                listOf(
                    appModule,
                    dispatcherModule,
                    imageModule,
                    networkModule,
                    storageModule
                )
            )
        }
    }
}
