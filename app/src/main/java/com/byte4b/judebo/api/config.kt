package com.byte4b.judebo.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val secretKey = "DSFRGVergbewrbh"

fun getRetrofit(locale: String): Retrofit {
    return Retrofit.Builder().baseUrl("https://$locale.judebo.com/search_job/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun getAPI(locale: String) = getRetrofit(locale).create(API::class.java)

