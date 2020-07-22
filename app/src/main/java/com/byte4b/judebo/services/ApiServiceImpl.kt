package com.byte4b.judebo.services

import com.byte4b.judebo.api.getAPI
import com.byte4b.judebo.api.secretKey
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.view.ServiceListener
import com.google.gson.Gson
import com.google.gson.JsonObject
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
        getAPI(locale).getNearbyTargets("$northEastLatitude,$northEastLongitude",
        "$southWestLatitude,$southWestLongitude", secretKey)
            .enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    listener?.onNearbyMarkersLoaded(null)
                }

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        try {
                            val objects = response.body()?.entrySet()?.map {
                                Gson().fromJson(it.value, MyMarker::class.java)
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