package com.byte4b.judebo.api

import com.byte4b.judebo.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val secretKey = "DSFRGVergbewrbh"

fun getRetrofit(locale: String): Retrofit {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level =
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .addInterceptor(ResponseInterceptor())
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    return Retrofit.Builder().baseUrl("https://$locale.judebo.com")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
}

fun getAPI(locale: String) = getRetrofit(locale).create(API::class.java)

class ResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val modified = response.newBuilder()
            .removeHeader("Content-Type")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .build()

        return modified
    }
}
