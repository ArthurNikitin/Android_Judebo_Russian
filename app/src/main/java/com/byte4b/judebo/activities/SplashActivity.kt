package com.byte4b.judebo.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R
import com.byte4b.judebo.getLangFromLocale
import com.byte4b.judebo.models.JobType
import com.byte4b.judebo.models.Skill
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.google.gson.Gson
import java.util.*

class SplashActivity : AppCompatActivity(), ServiceListener {

    private var isSkillsLoaded = false
    private var isJobTypesLoaded = false
    private var isAnimationEnded = false
    private val isCanOpenMap get() = isJobTypesLoaded && isSkillsLoaded && isAnimationEnded

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        val setting = Setting(this)
        if (setting.language != "")
            setLocale(setting.language ?: "")
        else
            setLocale(getLangFromLocale().locale)

        if (setting.currency == "")
            setting.currency = setting.getCurrentCurrency().name

        Handler().postDelayed({
            isAnimationEnded = true
            if (isCanOpenMap)
                toNext()
        }, 1000)

        ApiServiceImpl(this).apply {
            val locale = setting.getCurrentLanguage().locale
            getSkills(locale)
            getJobTypes(locale)
        }
    }

    private fun toNext() {
        startActivity<MainActivity>()
        finish()
    }

    override fun onSkillsLoaded(list: List<Skill>?) {
        //save
        list?.forEach {
            Log.e("test", Gson().toJson(it))
        }

        isSkillsLoaded = true
        if (isCanOpenMap)
            toNext()
    }

    override fun onJobTypesLoaded(list: List<JobType>?) {
        //save
        list?.forEach {
            Log.e("test", Gson().toJson(it))
        }

        isJobTypesLoaded = true
        if (isCanOpenMap)
            toNext()
    }

    private fun setLocale(locale: String) {
        resources.apply {
            configuration.setLocale(Locale(locale))
            updateConfiguration(configuration, displayMetrics)
        }
    }

}