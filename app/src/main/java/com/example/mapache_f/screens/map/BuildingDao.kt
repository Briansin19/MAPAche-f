package com.example.mapache_f.screens.map

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings")
    fun getAllBuildings(): List<BuildingEntity>

    @Insert
    fun insertBuildings(buildings: List<BuildingEntity>)

    @Query("DELETE FROM buildings")
    fun clear()
}