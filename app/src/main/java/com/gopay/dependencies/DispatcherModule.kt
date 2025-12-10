package com.gopay.dependencies

import com.gopay.dispatcher.CoroutineDispatcherProvider
import com.gopay.dispatcher.RealCoroutineDispatcherProvider
import org.koin.dsl.module

val dispatcherModule = module {
    single<CoroutineDispatcherProvider> {
        RealCoroutineDispatcherProvider()
    }
}
