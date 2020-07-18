package com.byte4b.judebo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import java.util.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val setting = Setting(this)
        if (setting.language != "")
            setLocale(setting.language ?: "")
        else
            setLocale("en")

        startActivity<MainActivity>()
        finish()
    }

    private fun setLocale(locale: String) {
        resources.configuration.locale = Locale(locale)
        resources.updateConfiguration(
            resources.configuration,
            resources.displayMetrics
        )
    }

}