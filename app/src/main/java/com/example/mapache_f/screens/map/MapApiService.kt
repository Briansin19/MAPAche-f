package com.example.mapache_f.screens.map

import retrofit2.Call
import retrofit2.http.GET

// Clases de datos para el JSON
data class UserLocation(val lat: Double, val lng: Double)
data class BoundsCoordinate(val lat: Double, val lng: Double)
data class MapDataResponse(
    val user_location: UserLocation,
    val bounds: List<BoundsCoordinate>
)

interface MapApiService {
    @GET("mapdata")
    fun getMapData(): Call<MapDataResponse>
}
