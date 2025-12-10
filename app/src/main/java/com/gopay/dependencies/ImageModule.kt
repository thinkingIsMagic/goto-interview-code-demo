package com.gopay.dependencies

import android.content.Context
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import org.koin.dsl.module

/**
 * 图片加载模块，统一创建 Picasso 实例（共享 OkHttp 缓存配置）。
 */
val imageModule = module {
    single<Picasso> {
        providePicasso(get())
    }
}

// 单独拆出方便未来替换为 Coil/Glide，或覆写下载器配置。
private fun providePicasso(context: Context) = Picasso.Builder(context)
    .downloader(OkHttp3Downloader(context))
    .build()
