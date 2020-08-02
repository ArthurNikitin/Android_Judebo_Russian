package com.byte4b.judebo.activities

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import java.util.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        val setting = Setting(this)
        if (setting.language != "")
            setLocale(setting.language ?: "")
        else
            setLocale("en")

        Handler().postDelayed({
            startActivity<MainActivity>()
            finish()
        }, 1000)

    }

    private fun setLocale(locale: String) {
        resources.apply {
            configuration.setLocale(Locale(locale))
            updateConfiguration(configuration, displayMetrics)
        }
    }

}