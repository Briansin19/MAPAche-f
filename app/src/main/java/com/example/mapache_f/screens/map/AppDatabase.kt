package com.example.mapache_f.screens.map

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BuildingEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun buildingDao(): BuildingDao
}