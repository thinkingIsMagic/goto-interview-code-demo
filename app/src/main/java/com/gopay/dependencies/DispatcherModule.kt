package com.gopay.dependencies

import com.gopay.dispatcher.CoroutineDispatcherProvider
import com.gopay.dispatcher.RealCoroutineDispatcherProvider
import org.koin.dsl.module

/**
 * 协程调度器注入模块，提供可替换的 DispatcherProvider，
 * 便于在单元测试中注入 TestDispatcher。
 */
val dispatcherModule = module {
    single<CoroutineDispatcherProvider> {
        RealCoroutineDispatcherProvider()
    }
}
