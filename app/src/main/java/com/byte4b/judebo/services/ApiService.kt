package com.byte4b.judebo.services


interface ApiService {

    fun getNearbyMarkers(
        locale: String,
        northEastLatitude: Double,
        northEastLongitude: Double,
        southWestLatitude: Double,
        southWestLongitude: Double
    )

    fun getSkills(locale: String)

    fun getJobTypes(locale: String)

    fun getRates(locale: String)

}