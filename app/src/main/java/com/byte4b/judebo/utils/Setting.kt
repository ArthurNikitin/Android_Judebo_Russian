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
                pref.getFloat("lastMapCameraPosition_latitude", defaultLatitude.toFloat())
                    .toDouble(),
                pref.getFloat("lastMapCameraPosition_longitude", defaultLongitude.toFloat())
                    .toDouble()
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

    val max_search_latitude_size = 0.4
    val max_search_longitude_size = 0.4
    val search_square_increase_infinitesimal = 0.1
    val search_request_pause = 1 //seconds
    val search_request_min_move_delta = 0.1 //degrees

    val cluster_radius = 60.0
    val cluster_sizes =
        listOf(2..3, 4..5, 6..10, 11..15, 16..20, 21..25, 26..30, 31..35, 36..40, 41..45)

}