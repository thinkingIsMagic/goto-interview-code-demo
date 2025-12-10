package com.gopay.extensions

import androidx.lifecycle.MutableLiveData

/**
 * 便捷控制 Loading 类布尔 LiveData 的扩展。
 */
fun MutableLiveData<Boolean>.show() = this.postValue(true)

fun MutableLiveData<Boolean>.hide() = this.postValue(false)
