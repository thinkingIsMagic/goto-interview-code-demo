package com.gopay.dependencies

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gopay.persistance.AppDatabase
import org.koin.dsl.module

val storageModule = module {
    single<SharedPreferences> {
        PreferenceManager.getDefaultSharedPreferences(get())
    }
    single<RoomDatabase> {
        Room.databaseBuilder(get(), AppDatabase::class.java, "app_db").build()
    }
}
