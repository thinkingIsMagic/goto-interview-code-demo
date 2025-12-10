package com.gopay.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * 抽象调度器提供者，便于业务层依赖注入不同环境的 Dispatchers。
 * 测试时可提供 TestCoroutineDispatcher 替换。
 */
interface CoroutineDispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfirmed: CoroutineDispatcher
}
