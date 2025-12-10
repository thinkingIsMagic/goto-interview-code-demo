package com.gopay.dependencies

import android.content.Context
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import org.koin.dsl.module

/**
 * 图片加载模块
 * 
 * 这个模块配置了Picasso图片加载库。
 * Picasso是Square公司开发的图片加载库，用于：
 * - 从网络加载图片
 * - 从本地文件加载图片
 * - 图片缓存管理
 * - 图片转换（缩放、裁剪等）
 * 
 * 配置说明：
 * - 使用OkHttp3Downloader作为下载器，这样可以复用OkHttp的缓存机制
 * - 使用single：整个应用只需要一个Picasso实例（单例）
 */
val imageModule = module {
    /**
     * Picasso实例
     * 
     * Picasso是图片加载库，提供了简单的API来加载和显示图片。
     * 使用示例：
     * Picasso.get().load("https://example.com/image.jpg").into(imageView)
     */
    single<Picasso> {
        providePicasso(get())
    }
}

/**
 * 提供Picasso实例
 * 
 * 配置Picasso使用OkHttp3Downloader作为下载器。
 * 这样做的好处：
 * - 复用OkHttp的缓存机制
 * - 统一的网络请求管理
 * - 更好的性能
 * 
 * @param context Android上下文
 * @return 配置好的Picasso实例
 */
private fun providePicasso(context: Context) = Picasso.Builder(context)
    .downloader(OkHttp3Downloader(context))  // 使用OkHttp3作为下载器
    .build()
