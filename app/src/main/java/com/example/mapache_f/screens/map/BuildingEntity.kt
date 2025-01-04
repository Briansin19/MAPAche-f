package com.example.mapache_f.screens.map

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey val name: String,
    val lat: Double,
    val lng: Double
)