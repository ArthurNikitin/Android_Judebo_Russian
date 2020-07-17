package com.byte4b.judebo.utils

import android.content.Context

class Setting(ctx: Context) {

    private val pref = ctx.getSharedPreferences("setting", Context.MODE_PRIVATE)

    var language
        get() = pref.getString("language", "")
        set(value) = pref.edit().putString("language", value).apply()

}