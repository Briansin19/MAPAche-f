package com.example.mapache_f.screens.map

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings")
    fun getAllBuildings(): List<BuildingEntity>

    @Query("SELECT * FROM buildings WHERE name = :buildingName LIMIT 1")
    fun getBuildingByName(buildingName: String): BuildingEntity?

    @Insert
    fun insertBuildings(buildings: List<BuildingEntity>)

    @Delete
    fun deleteBuilding(building: BuildingEntity)

    @Query("DELETE FROM buildings")
    fun clear()

    @Update
    fun updateBuilding(building: BuildingEntity): Int
}