package com.byte4b.judebo.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val secretKey = "DSFRGVergbewrbh"

fun getRetrofit(locale: String): Retrofit {
    val gson = GsonBuilder().setLenient().create()

    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY


    val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()


    return Retrofit.Builder().baseUrl("https://$locale.judebo.com/search_job/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()
}

fun getAPI(locale: String) = getRetrofit(locale).create(API::class.java)

