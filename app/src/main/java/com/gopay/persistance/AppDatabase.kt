package com.gopay.persistance

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

/**
 * 示例用户实体，用于展示 Room 配置与注解写法。
 */
@Entity
data class ExampleUser(
    @PrimaryKey val id: Int,
    val firstName: String?,
    val lastName: String?
)

/**
 * 示例数据库定义，后续可在此添加 Dao 抽象方法。
 */
@Database(entities = [ExampleUser::class], version = 1)
abstract class AppDatabase : RoomDatabase()