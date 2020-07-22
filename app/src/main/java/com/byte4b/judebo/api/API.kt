package com.byte4b.judebo.api

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface API {

    @GET("/search_job/app.php")
    fun getNearbyTargets(@Query("NorthEast") northEastLatLon: String,
                         @Query("SouthWest") southWestLatLon: String,
                         @Query("key") secretKey: String):
            Call<JsonObject>

}