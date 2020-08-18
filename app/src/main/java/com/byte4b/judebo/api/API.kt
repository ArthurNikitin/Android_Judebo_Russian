package com.byte4b.judebo.api

import com.byte4b.judebo.models.JobType
import com.byte4b.judebo.models.Skill
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

    @GET("/search_job/app_skills_list.php")
    fun getSkills(@Query("key") secretKey: String):
            Call<List<Skill>>

    @GET("/search_job/app_jobs_types.php")
    fun getJobTypes(@Query("key") secretKey: String):
            Call<List<JobType>>
}