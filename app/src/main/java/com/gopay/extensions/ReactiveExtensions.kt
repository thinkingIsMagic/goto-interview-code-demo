package com.gopay.extensions

import androidx.lifecycle.MutableLiveData

/**
 * 响应式编程扩展函数集合
 * 
 * 这些扩展函数简化了LiveData的常用操作。
 * LiveData是Android Architecture Components的一部分，用于实现观察者模式。
 */

/**
 * 显示（设置为true）
 * 
 * 将MutableLiveData<Boolean>的值设置为true。
 * 使用postValue而不是setValue，因为postValue可以在任何线程调用，
 * 而setValue必须在主线程调用。
 * 
 * 使用场景：
 * - 显示加载指示器：loadingLiveData.show()
 * - 显示错误提示：errorLiveData.show()
 * - 显示对话框：dialogLiveData.show()
 * 
 * 使用示例：
 * val isLoading = MutableLiveData<Boolean>()
 * isLoading.show()  // 等同于 isLoading.postValue(true)
 */
fun MutableLiveData<Boolean>.show() = this.postValue(true)

/**
 * 隐藏（设置为false）
 * 
 * 将MutableLiveData<Boolean>的值设置为false。
 * 使用postValue而不是setValue，因为postValue可以在任何线程调用。
 * 
 * 使用场景：
 * - 隐藏加载指示器：loadingLiveData.hide()
 * - 隐藏错误提示：errorLiveData.hide()
 * - 隐藏对话框：dialogLiveData.hide()
 * 
 * 使用示例：
 * val isLoading = MutableLiveData<Boolean>()
 * isLoading.hide()  // 等同于 isLoading.postValue(false)
 */
fun MutableLiveData<Boolean>.hide() = this.postValue(false)
