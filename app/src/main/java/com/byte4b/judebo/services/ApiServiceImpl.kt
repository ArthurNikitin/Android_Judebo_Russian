package com.byte4b.judebo.services

import android.util.Log
import com.byte4b.judebo.api.getAPI
import com.byte4b.judebo.api.secretKey
import com.byte4b.judebo.models.*
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class ApiServiceImpl(val listener: ServiceListener?) : ApiService {

    private fun check(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            Log.e("test", e.localizedMessage ?: "Error")
        }
    }

    override fun getNearbyMarkers(
        locale: String,
        northEastLatitude: Double,
        northEastLongitude: Double,
        southWestLatitude: Double,
        southWestLongitude: Double
    ) {
        getAPI(locale).getNearbyTargets("$northEastLatitude,$northEastLongitude",
        "$southWestLatitude,$southWestLongitude", secretKey)
            .enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    check { listener?.onNearbyMarkersLoaded(null) }
                }

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        try {
                            val objects = response.body()?.entrySet()?.map {
                                val result = Gson().fromJson(it.value, MyMarker::class.java)

                                result.UF_MAP_POINT_LATITUDE +=
                                    Random.nextInt(-8, 8) * Setting.VALUE_INFINITESIMAL

                                result.UF_MAP_POINT_LONGITUDE +=
                                    2 * Random.nextInt(-8, 8) * Setting.VALUE_INFINITESIMAL

                                result
                            }
                            listener?.onNearbyMarkersLoaded(objects)
                        } catch (e: Exception) {
                            check { listener?.onNearbyMarkersLoaded(null) }
                        }
                    } else
                        check { listener?.onNearbyMarkersLoaded(null) }
                }

            })
    }

    override fun getSkills(locale: String) {
        getAPI(locale)
            .getSkills(secretKey)
            .enqueue(object : Callback<List<Skill>> {
                override fun onFailure(call: Call<List<Skill>>, t: Throwable) {
                    listener?.onSkillsLoaded(null)
                }

                override fun onResponse(call: Call<List<Skill>>, response: Response<List<Skill>>) {
                    if (response.isSuccessful)
                        listener?.onSkillsLoaded(response.body())
                    else
                        listener?.onSkillsLoaded(null)
                }
            })
    }

    override fun getJobTypes(locale: String) {
        getAPI(locale)
            .getJobTypes(secretKey)
            .enqueue(object : Callback<List<JobType>> {
                override fun onFailure(call: Call<List<JobType>>, t: Throwable) {
                    listener?.onJobTypesLoaded(null)
                }

                override fun onResponse(call: Call<List<JobType>>, response: Response<List<JobType>>) {
                    if (response.isSuccessful)
                        listener?.onJobTypesLoaded(response.body())
                    else
                        listener?.onJobTypesLoaded(null)
                }
            })
    }

    override fun getRates(locale: String) {
        getAPI(locale)
            .getRates(secretKey)
            .enqueue(object : Callback<List<CurrencyRate>> {
                override fun onFailure(call: Call<List<CurrencyRate>>, t: Throwable) {
                    listener?.onRatesLoaded(null)
                }

                override fun onResponse(call: Call<List<CurrencyRate>>, response: Response<List<CurrencyRate>>) {
                    if (response.isSuccessful)
                        listener?.onRatesLoaded(response.body())
                    else
                        listener?.onRatesLoaded(null)
                }
            })
    }

    override fun getMyVocations(locale: String, token: String, login: String) {
        getAPI(locale)
            .getMyVocations(secretKey, token, login)
            .enqueue(object : Callback<List<Vocation>> {
                override fun onFailure(call: Call<List<Vocation>>, t: Throwable) {
                    check { listener?.onMyVocationsLoaded(null) }
                    Log.e("test", t.localizedMessage ?: "fail")
                    Log.e("test", call.request().url().toString())

                }

                override fun onResponse(call: Call<List<Vocation>>, response: Response<List<Vocation>>) {
                    Log.e("test", call.request().url().toString())
                    check {
                        if (response.isSuccessful)
                            listener?.onMyVocationsLoaded(response.body())
                        else
                            listener?.onMyVocationsLoaded(null)
                    }
                }
            })
    }

    override fun deleteVocation(locale: String, token: String, login: String, vocation: Vocation) {
        getAPI(locale)
            .deleteVocation(secretKey, token, login, listOf(vocation))
            .enqueue(object : Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Log.e("test", t.localizedMessage ?: "onFail")
                    check { listener?.onVocationDeleted(false) }
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    Log.e("test", Gson().toJson(response.body()))
                    check {
                        if (response.isSuccessful)
                            listener?.onVocationDeleted((response.body() as String).contains("success"))
                        else
                            listener?.onVocationDeleted(false)
                    }
                }
            })
    }

    override fun updateMyVocations(
        locale: String,
        token: String,
        login: String,
        vocations: List<Vocation>
    ) {
        getAPI(locale)
            .updateMyVocations(secretKey, token, login, vocations)
        //todo:
    }

    override fun addMyVocation(locale: String, token: String, login: String, vocation: Vocation) {
        getAPI(locale)
            .addVocation(secretKey, token, login, listOf(vocation))
            //.enqueue(object : Callback<Any> )
        //todo:
    }

}