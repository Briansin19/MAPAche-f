package com.example.mapache_f.screens.map

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class ORSGeometry(val coordinates: List<List<Double>>)
data class ORSStep(val instruction: String)
data class ORSSegment(val steps: List<ORSStep>)
data class ORSProperties(val segments: List<ORSSegment>)
data class ORSFeature(val geometry: ORSGeometry, val properties: ORSProperties, val type: String)
data class ORSResponse(val features: List<ORSFeature>, val type: String)

interface OpenRouteServiceApi {
    @GET("v2/directions/wheelchair")
    fun getDirections(
        @Query("api_key") apiKey: String,   // Aquí va el NOMBRE del parámetro, no el valor
        @Query("start") start: String,      // "lng,lat"
        @Query("end") end: String,          // "lng,lat"
        @Query("language") language: String = "es"
    ): Call<ORSResponse>
}
