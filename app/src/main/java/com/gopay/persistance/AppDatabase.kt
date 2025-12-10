package com.gopay.persistance

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

/**
 * 示例用户实体类
 * 
 * 这是一个Room数据库的实体类示例。
 * Room使用注解来定义数据库表结构。
 * 
 * @Entity：标记这是一个数据库实体（表）
 * @PrimaryKey：标记主键字段
 * 
 * 注意：这是一个示例类，实际项目中应该根据业务需求定义实体类
 */
@Entity
data class ExampleUser(
    /** 用户ID，主键 */
    @PrimaryKey val id: Int,
    /** 用户名字 */
    val firstName: String?,
    /** 用户姓氏 */
    val lastName: String?
)

/**
 * 应用数据库
 * 
 * Room数据库的抽象类，定义了：
 * - 数据库包含哪些实体（表）
 * - 数据库版本号
 * 
 * Room会自动生成这个类的实现。
 * 
 * @Database注解说明：
 * - entities：指定数据库包含的实体类列表
 * - version：数据库版本号，当数据库结构改变时需要增加版本号
 * 
 * 使用说明：
 * 1. 定义DAO（Data Access Object）接口来访问数据库
 * 2. 在StorageModule中创建数据库实例
 * 3. 通过DAO接口进行数据库操作
 */
@Database(entities = [ExampleUser::class], version = 1)
abstract class AppDatabase : RoomDatabase {
    // 在这里可以定义DAO接口的抽象方法
    // 例如：abstract fun userDao(): UserDao
}()