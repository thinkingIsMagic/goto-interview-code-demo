package com.gopay.dependencies

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gopay.persistance.AppDatabase
import org.koin.dsl.module

/**
 * 存储模块
 * 
 * 这个模块配置了应用的数据存储相关依赖：
 * - SharedPreferences：用于存储简单的键值对数据（如用户设置、配置等）
 * - Room数据库：用于存储结构化数据（如用户信息、业务数据等）
 * 
 * 使用single：这些对象在整个应用生命周期中只需要一个实例（单例模式）
 */
val storageModule = module {
    /**
     * SharedPreferences实例
     * 
     * SharedPreferences是Android提供的轻量级数据存储方案，
     * 适合存储简单的键值对数据，如：
     * - 用户偏好设置
     * - 登录状态
     * - 简单的配置信息
     * 
     * 使用默认的SharedPreferences，存储在应用的私有目录中
     */
    single<SharedPreferences> {
        PreferenceManager.getDefaultSharedPreferences(get())
    }
    
    /**
     * Room数据库实例
     * 
     * Room是Google提供的SQLite数据库抽象层，提供了：
     * - 编译时SQL验证
     * - 类型安全的数据库访问
     * - 与LiveData和RxJava的集成
     * 
     * 数据库名称：app_db
     * 数据库类：AppDatabase
     * 
     * 注意：这里返回的是RoomDatabase基类，实际使用时应该注入具体的AppDatabase类型
     */
    single<RoomDatabase> {
        Room.databaseBuilder(get(), AppDatabase::class.java, "app_db").build()
    }
}
