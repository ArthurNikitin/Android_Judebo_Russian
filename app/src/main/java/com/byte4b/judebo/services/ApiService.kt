package com.byte4b.judebo.services

import com.byte4b.judebo.models.Vocation


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

    fun getMyVocations(locale: String, token: String, login: String)

    fun deleteVocation(locale: String, token: String, login: String, vocation: Vocation)

    fun updateMyVocations(locale: String, token: String, login: String, vocations: List<Vocation>)

    fun addMyVocation(locale: String, token: String, login: String, vocation: Vocation)

    fun signInWithEmail(locale: String, login: String, password: String)

    fun signInWithFb(locale: String, login: String)

    fun signInWithGoogle(locale: String, login: String)

    fun signUpWithEmail(locale: String, login: String, password: String)

    fun signUpWithFb(locale: String, login: String, password: String)

    fun signUpWithGoogle(locale: String, login: String, password: String)

}