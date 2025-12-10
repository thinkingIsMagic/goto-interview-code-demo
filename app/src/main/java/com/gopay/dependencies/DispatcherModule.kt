package com.gopay.dependencies

import com.gopay.dispatcher.CoroutineDispatcherProvider
import com.gopay.dispatcher.RealCoroutineDispatcherProvider
import org.koin.dsl.module

/**
 * 协程调度器模块
 * 
 * 这个模块配置了Kotlin协程的调度器提供者。
 * 协程调度器用于控制协程在哪个线程上执行。
 * 
 * 为什么需要这个模块？
 * - 便于测试：可以注入测试用的调度器
 * - 统一管理：所有协程调度都通过这个接口
 * - 解耦：业务代码不直接依赖Dispatchers，而是依赖接口
 * 
 * 使用single：整个应用只需要一个调度器提供者实例
 */
val dispatcherModule = module {
    /**
     * 协程调度器提供者
     * 
     * 提供不同类型的协程调度器：
     * - Main：主线程，用于UI操作
     * - IO：IO线程池，用于网络请求、文件操作等
     * - Default：默认线程池，用于CPU密集型任务
     * - Unconfined：不限制线程，不推荐使用
     */
    single<CoroutineDispatcherProvider> {
        RealCoroutineDispatcherProvider()
    }
}
