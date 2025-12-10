package com.gopay.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * 生产环境默认实现，直接返回 Kotlin 标准 Dispatchers。
 */
open class RealCoroutineDispatcherProvider : CoroutineDispatcherProvider {
    override val main: CoroutineDispatcher by lazy { Dispatchers.Main }
    override val io: CoroutineDispatcher by lazy { Dispatchers.IO }
    override val default: CoroutineDispatcher by lazy { Dispatchers.Default }
    override val unconfirmed: CoroutineDispatcher by lazy { Dispatchers.Unconfined }
}
