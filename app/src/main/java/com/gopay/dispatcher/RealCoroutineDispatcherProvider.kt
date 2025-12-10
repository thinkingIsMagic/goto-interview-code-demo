package com.gopay.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * 真实的协程调度器提供者实现
 * 
 * 这是CoroutineDispatcherProvider接口的具体实现。
 * 使用Kotlin标准库中的Dispatchers来提供各种调度器。
 * 
 * 使用lazy延迟初始化：
 * - 只有在第一次访问时才会创建调度器
 * - 提高应用启动速度
 * - 节省内存（如果某个调度器从未使用）
 * 
 * 使用open关键字：
 * - 允许在测试中创建子类来提供测试用的调度器
 */
open class RealCoroutineDispatcherProvider : CoroutineDispatcherProvider {
    /**
     * 主线程调度器
     * 用于UI操作，如更新TextView、ImageView等
     */
    override val main: CoroutineDispatcher by lazy { Dispatchers.Main }
    
    /**
     * IO线程池调度器
     * 用于：
     * - 网络请求
     * - 文件读写
     * - 数据库操作
     * - 其他IO密集型任务
     */
    override val io: CoroutineDispatcher by lazy { Dispatchers.IO }
    
    /**
     * 默认线程池调度器
     * 用于：
     * - CPU密集型计算
     * - 数据排序
     * - 图像处理
     * - 其他需要CPU资源的任务
     */
    override val default: CoroutineDispatcher by lazy { Dispatchers.Default }
    
    /**
     * 未限制的调度器
     * 不推荐在生产环境使用，主要用于测试或特殊场景
     */
    override val unconfirmed: CoroutineDispatcher by lazy { Dispatchers.Unconfined }
}
