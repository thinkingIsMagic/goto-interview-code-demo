package com.gopay.persistance

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

@Entity
data class ExampleUser(
    @PrimaryKey val id: Int,
    val firstName: String?,
    val lastName: String?
)

@Database(entities = [ExampleUser::class], version = 1)
abstract class AppDatabase : RoomDatabase()