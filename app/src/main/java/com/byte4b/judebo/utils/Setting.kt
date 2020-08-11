package com.byte4b.judebo.utils

import android.content.Context
import androidx.core.content.edit
import com.byte4b.judebo.getLangFromLocale
import com.byte4b.judebo.models.Currency
import com.byte4b.judebo.models.Language
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.models.languages
import com.google.android.gms.maps.model.LatLng

class Setting(ctx: Context) {

    private val pref = ctx.getSharedPreferences("setting", Context.MODE_PRIVATE)

    var language
        get() = pref.getString("language", "")
        set(value) = pref.edit().putString("language", value).apply()

    var currency
        get() = pref.getString("currency", "")
        set(value) = pref.edit().putString("currency", value).apply()

    var isFromRecreate
        get() = pref.getBoolean("is_from_recreate", false)
        set(value) = pref.edit().putBoolean("is_from_recreate", value).apply()

    var lastMapCameraPosition: LatLng
        get() {
            return LatLng(
                pref.getFloat("lastMapCameraPosition_latitude", DEFAULT_LATITUDE.toFloat())
                    .toDouble(),
                pref.getFloat("lastMapCameraPosition_longitude", DEFAULT_LONGITUDE.toFloat())
                    .toDouble()
            )
        }
        set(value) {
            pref.edit {
                putFloat("lastMapCameraPosition_latitude", value.latitude.toFloat())
                putFloat("lastMapCameraPosition_longitude", value.longitude.toFloat())
            }
        }

    fun getCurrentLanguage(): Language {
        return if (language == "") getLangFromLocale()
        else languages.first { it.locale == language!! }
    }

    fun getCurrentCurrency(): Currency {
        return if (currency == "") {
            if (language == "")
                currencies.firstOrNull { it.name == getLangFromLocale().currency } ?: currencies.first()
            else
                currencies.firstOrNull { it.name == currency } ?: currencies.first()
        } else
            currencies.firstOrNull { it.name == currency } ?: currencies.first()
    }

    companion object {
        const val BASIC_ZOOM = 11.0f
        const val MIN_ZOOM = 2.0f
        const val MAX_ZOOM = 21.0f

        const val DEFAULT_LATITUDE = 0.0
        const val DEFAULT_LONGITUDE = 0.0

        const val MAX_SEARCH_LATITUDE_SIZE = 0.3
        const val MAX_SEARCH_LONGITUDE_SIZE = 0.3

        const val SEARCH_REQUEST_PAUSE_SECONDS = 1
        const val SEARCH_REQUEST_MIN_MOVE_DELTA = 0.18
        //degrees

        // 2020-08-10 b.2.5
        // const val CLUSTER_RADIUS = 165.0
        const val CLUSTER_RADIUS = 100.0
        const val VALUE_INFINITESIMAL = 0.00001
        const val CURRENCY_ICON_SIZE = 24

        val CLUSTER_SIZES =
            listOf(2..3, 4..5, 6..7, 8..9, 10..11, 12..15, 16..20, 21..30, 31..50, 51..99)
    }

}