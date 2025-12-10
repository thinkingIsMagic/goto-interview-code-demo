package com.gopay.dependencies

import android.content.Context
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import org.koin.dsl.module

val imageModule = module {
    single<Picasso> {
        providePicasso(get())
    }
}

private fun providePicasso(context: Context) = Picasso.Builder(context)
    .downloader(OkHttp3Downloader(context))
    .build()
