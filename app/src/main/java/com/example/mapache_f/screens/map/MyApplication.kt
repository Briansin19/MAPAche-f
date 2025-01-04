package com.example.mapache_f.screens.map

import android.app.Application
import androidx.room.Room

class MyApplication : Application() {
    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "mapdatabase.db"
        ).build()
    }
}