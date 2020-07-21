package com.byte4b.judebo.view

import com.byte4b.judebo.models.MyMarker

interface ServiceListener {

    fun onNearbyMarkersLoaded(list: List<MyMarker>?) {}

}