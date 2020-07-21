package com.byte4b.judebo.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Path

interface API {

    @GET("{locale}.judebo.com/search_job/app.php")
    fun getNearbyTargets(@Path("locale") locale: String,
                         @Field("NorthEast") northEastLatLon: String,
                         @Field("SouthWest") southWestLatLon: String,
                         @Field("key") secretKey: String):
            Call<String>

}