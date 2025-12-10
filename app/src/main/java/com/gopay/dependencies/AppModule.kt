package com.gopay.dependencies

import org.koin.dsl.module

/**
 * 应用模块
 * 
 * 这是Koin依赖注入的应用模块。
 * 当前为空模块，可以根据需要在这里添加应用级别的依赖。
 * 
 * 使用场景：
 * - 应用级别的单例对象
 * - 全局配置
 * - 应用级别的工具类
 * 
 * 示例用法：
 * single<AppConfig> { AppConfig() }
 * single<AppRepository> { AppRepository() }
 */
val appModule = module {
    // 当前为空，可以根据需要添加应用级别的依赖
}
