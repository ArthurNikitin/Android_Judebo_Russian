package com.byte4b.judebo.api

import com.byte4b.judebo.models.*
import com.byte4b.judebo.models.request.CreateSkillRequest
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

    @GET("app_auth.php")
    fun signInWithEmail(@Query("login") login: String,
                        @Query("pass") password: String,
                        @Query("key") key: String = secretKey):
            Call<AuthResult>

    @GET("app_auth.php")
    fun signInWithFb(@Query("login") login: String,
                     @Query("fb") fb: Int = 1,
                     @Query("key") key: String = secretKey):
            Call<AuthResult>

    @GET("app_auth.php")
    fun signInWithGoogle(@Query("login") login: String,
                         @Query("gg") fb: Int = 1,
                         @Query("key") key: String = secretKey):
            Call<AuthResult>

    @GET("app_reg_user.php")
    fun signUpWithEmail(@Query("login") login: String,
                        @Query("pass") password: String,
                        @Query("key") key: String = secretKey):
            Call<AuthResult>

    @GET("app_reg_user.php")
    fun signUpWithFb(@Query("login") login: String,
                     @Query("fb") fb: Int = 1,
                     @Query("pass") password: String,
                     @Query("key") key: String = secretKey):
            Call<AuthResult>

    @GET("app_reg_user.php")
    fun signUpWithGoogle(@Query("login") login: String,
                         @Query("gg") fb: Int = 1,
                         @Query("pass") password: String,
                         @Query("key") key: String = secretKey):
            Call<AuthResult>

    @GET("app_user_delete.php")
    fun deleteMe(@Query("key") secretKey: String,
                 @Query("token") token: String,
                 @Query("login") login: String):
            Call<Result>

    @POST("app_add_tag.php")
    fun createSkill(@Query("key") secretKey: String,
                    @Query("tok") token: String,
                    @Query("login") login: String,
                    @Body listsSingleSkill: List<CreateSkillRequest>):
            Call<AuthResult>

    @GET("app_user_subs.php")
    fun setMySubs(@Query("key") secretKey: String,
                  @Query("tok") token: String,
                  @Query("login") login: String,
                  @Query("subsid") subsId: String?,
                  @Query("subsend") subsEnd: String?,
                  @Query("storetoken") storeToken: String?):
            Call<AuthResult>

    @GET("app_user_have_subs.php")
    fun getMySub(@Query("key") secretKey: String,
                 @Query("tok") token: String,
                 @Query("login") login: String):
            Call<SubAnswer>

    @GET("app_subs_list.php")
    fun getSubscriptions(@Query("key") secretKey: String):
            Call<List<Subscription>>

    @GET("app_adv.php?lang=ru&type=1&key=DSFRGVergbewrbh")
    fun loadAd(@Query("key") secretKey: String,
               @Query("lang") locale: String,
               @Query("type") mediaType: Int):
            Call<CustomAd>

}