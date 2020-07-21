package com.byte4b.judebo.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.byte4b.judebo.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://"
const val secretKey = "DSFRGVergbewrbh"

fun getRetrofit(locale: String): Retrofit {
    return Retrofit.Builder().baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun getAPI() = getRetrofit().create(API::class.java)

fun onError(ctx: Context, reason: String) {
    try {
        when (reason) {
            else -> Log.e("debug", "new type error: $reason")
        }
    } catch (e: Exception) {}
}

fun onFailure(context: Context, t: Throwable) {
    try {
        if (t.message!!.contains("No address associated with hostname"))
            Toast.makeText(context, R.string.error_no_internet, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {}
}
