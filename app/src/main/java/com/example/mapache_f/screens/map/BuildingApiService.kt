package com.example.mapache_f.screens.map

import retrofit2.Call
import retrofit2.http.GET

data class Building(val name: String, val lat: Double, val lng: Double)

interface BuildingApiService {
    @GET("buildings")
    fun getBuildings(): Call<List<Building>>
}
