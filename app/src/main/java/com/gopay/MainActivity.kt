package com.gopay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * 主Activity
 * 
 * 这是应用的入口Activity，在AndroidManifest.xml中被配置为启动Activity。
 * 当用户打开应用时，系统会启动这个Activity。
 * 
 * 当前实现：
 * - 简单地加载activity_main布局文件
 * - 这是面试项目的基架，可以根据需求扩展功能
 */
class MainActivity : AppCompatActivity() {

    /**
     * Activity创建时的回调方法
     * 
     * @param savedInstanceState 保存的实例状态，用于Activity重建时恢复数据
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置Activity的布局文件
        // R.layout.activity_main 对应 res/layout/activity_main.xml
        setContentView(R.layout.activity_main)
    }
}
