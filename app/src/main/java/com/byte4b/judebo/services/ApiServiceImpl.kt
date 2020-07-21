package com.byte4b.judebo.services

import com.byte4b.judebo.api.getAPI
import com.byte4b.judebo.api.secretKey
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.view.ServiceListener
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiServiceImpl(val listener: ServiceListener?) : ApiService {

    override fun getNearbyMarkers(
        locale: String,
        northEastLatitude: Double,
        northEastLongitude: Double,
        southWestLatitude: Double,
        southWestLongitude: Double
    ) {
        getAPI().getNearbyTargets(locale, "$northEastLatitude,$northEastLongitude",
        "$southWestLatitude,$southWestLongitude", secretKey)
            .enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    listener?.onNearbyMarkersLoaded(null)
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        try {
                            val answer = response.body() ?: "{}"
                            val objects = answer.substring(1, answer.length - 2).split(",")
                                .map {
                                    val data = it.substring(it.indexOf("{"))
                                    Gson().fromJson(data, MyMarker::class.java)
                                }
                            listener?.onNearbyMarkersLoaded(objects)
                        } catch (e: Exception) {
                            listener?.onNearbyMarkersLoaded(null)
                        }
                    } else
                        listener?.onNearbyMarkersLoaded(null)
                }

            })
    }

}