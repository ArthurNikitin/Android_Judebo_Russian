package com.byte4b.judebo.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class AbstractMarker(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val marker: MyMarker
) : ClusterItem {

    override fun getSnippet(): String? = null
    override fun getTitle(): String? = null
    override fun getPosition() = LatLng(latitude, longitude)
}