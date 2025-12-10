package com.gopay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Demo 入口 Activity。当前仅负责加载布局，
 * 便于面试时在此扩展 Fragment、列表或其他业务页面。
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 绑定简单的占位布局，后续可在此添加导航或数据绑定逻辑
        setContentView(R.layout.activity_main)
    }
}
