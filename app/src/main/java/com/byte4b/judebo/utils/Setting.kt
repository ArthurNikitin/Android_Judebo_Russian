package com.byte4b.judebo.utils

import android.content.Context
import androidx.core.content.edit
import com.google.android.gms.maps.model.LatLng

class Setting(ctx: Context) {

    private val pref = ctx.getSharedPreferences("setting", Context.MODE_PRIVATE)

    var language
        get() = pref.getString("language", "")
        set(value) = pref.edit().putString("language", value).apply()

    var currency
        get() = pref.getString("currency", "")
        set(value) = pref.edit().putString("currency", value).apply()

    var lastMapCameraPosition: LatLng
        get() {
            return LatLng(
                pref.getFloat("lastMapCameraPosition_latitude", defaultLatitude.toFloat()).toDouble(),
                pref.getFloat("lastMapCameraPosition_longitude", defaultLongitude.toFloat()).toDouble()
            )
        }
        set(value) {
            pref.edit {
                putFloat("lastMapCameraPosition_latitude", value.latitude.toFloat())
                putFloat("lastMapCameraPosition_longitude", value.longitude.toFloat())
            }
        }

    val basicZoom = 11.0f
    val minZoom = 2.0f
    val maxZoom = 16.0f

    val defaultLatitude = 0.0
    val defaultLongitude = 0.0

}