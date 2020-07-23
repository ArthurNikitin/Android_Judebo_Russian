package com.byte4b.judebo.api

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface API {

    @GET("/search_job/app.php")
    fun getNearbyTargets(@Query(value = "NorthEast", encoded = false) northEastLatLon: String,
                         @Query(value = "SouthWest", encoded = false) southWestLatLon: String,
                         @Query("key") secretKey: String):
            Call<JsonObject>

}