package com.gopay.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * 协程调度器提供者接口
 * 
 * 这个接口定义了应用中需要的各种协程调度器。
 * 使用接口而不是直接使用Dispatchers的好处：
 * 1. 便于测试：可以在测试中注入Mock调度器
 * 2. 解耦：业务代码不直接依赖Dispatchers
 * 3. 灵活性：可以轻松切换不同的调度器实现
 * 
 * 协程调度器说明：
 * - main：主线程，用于UI操作（必须在主线程执行）
 * - io：IO线程池，用于网络请求、文件读写等IO操作
 * - default：默认线程池，用于CPU密集型任务（如计算、排序等）
 * - unconfirmed：不限制线程，不推荐在生产环境使用
 */
interface CoroutineDispatcherProvider {
    /** 主线程调度器，用于UI操作 */
    val main: CoroutineDispatcher
    
    /** IO线程池调度器，用于网络请求、文件操作等 */
    val io: CoroutineDispatcher
    
    /** 默认线程池调度器，用于CPU密集型任务 */
    val default: CoroutineDispatcher
    
    /** 未限制的调度器，不推荐使用 */
    val unconfirmed: CoroutineDispatcher
}
