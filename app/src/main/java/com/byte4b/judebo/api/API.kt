package com.byte4b.judebo.api

import com.byte4b.judebo.models.*
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface API {

    @GET("app.php")
    fun getNearbyTargets(@Query(value = "NorthEast", encoded = false) northEastLatLon: String,
                         @Query(value = "SouthWest", encoded = false) southWestLatLon: String,
                         @Query("key") secretKey: String):
            Call<JsonObject>

    @GET("app_skills_list.php")
    fun getSkills(@Query("key") secretKey: String):
            Call<List<Skill>>

    @GET("app_jobs_types.php")
    fun getJobTypes(@Query("key") secretKey: String):
            Call<List<JobType>>

    @GET("app_currency_rates.php")
    fun getRates(@Query("key") secretKey: String):
            Call<List<CurrencyRate>>

    @GET("app_user_jobs.php")
    fun getMyVocations(@Query("key") secretKey: String,
                       @Query("tok") token: String,
                       @Query("login") login: String):
            Call<List<Vocation>>

    @POST("app_add_job.php")
    fun deleteVocation(@Query("key") secretKey: String,
                       @Query("tok") token: String,
                       @Query("login") login: String,
                       @Body list: List<Vocation>):
            Call<Result>

    @POST("app_add_job.php")
    fun updateMyVocations(@Query("key") secretKey: String,
                          @Query("tok") token: String,
                          @Query("login") login: String,
                          @Body list: List<Vocation>):
            Call<Result>

    @POST("app_add_job.php")
    fun addVocation(@Query("key") secretKey: String,
                     @Query("tok") token: String,
                     @Query("login") login: String,
                     @Body list: List<Vocation>):
            Call<Result>

}