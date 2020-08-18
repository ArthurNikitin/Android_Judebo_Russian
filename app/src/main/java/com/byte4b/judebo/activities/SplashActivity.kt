package com.byte4b.judebo.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R
import com.byte4b.judebo.getLangFromLocale
import com.byte4b.judebo.models.JobType
import com.byte4b.judebo.models.JobTypeRealm
import com.byte4b.judebo.models.Skill
import com.byte4b.judebo.models.SkillRealm
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.google.gson.Gson
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.delete
import io.realm.kotlin.where
import java.util.*
import kotlin.time.milliseconds

class SplashActivity : AppCompatActivity(), ServiceListener {

    private val realm by lazy { Realm.getDefaultInstance() }

    private var isSkillsLoaded = false
    private var isJobTypesLoaded = false
    private var isAnimationEnded = false
    private val isCanOpenMap get() = isJobTypesLoaded && isSkillsLoaded && isAnimationEnded

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        Realm.init(this)
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
            val now = Calendar.getInstance().timeInMillis
            val lastUpdate = setting.lastUpdateDynamicDataFromServer.toLong()
            if (now > lastUpdate + Setting.PERIOD_UPDATE_DYNAMIC_DATA_FROM_SERVER_IN_MINUTE * 60 * 1000) {
                getSkills(locale)
                getJobTypes(locale)
            } else {
                isJobTypesLoaded = true
                isSkillsLoaded = true
                setting.lastUpdateDynamicDataFromServer = now.toString()
            }
        }
    }

    private fun toNext() {

        startActivity<MainActivity>()
        finish()
    }

    override fun onSkillsLoaded(list: List<Skill>?) {
        if (list != null) {
            try {
                realm.executeTransaction {
                    it.createObject<SkillRealm>()
                }
            } catch (e: Exception) {
                e.toLog("init")
            }
            try {
                realm.executeTransaction {
                    it.delete<SkillRealm>()
                    try {
                        it.createObject<SkillRealm>()
                    } catch (e: Exception) {
                        e.toLog("double")
                    }
                    it.copyToRealm(list.map { it.toRealmVersion() })
                }
            } catch (e: Exception) {
                e.toLog("long")
            }
        }

        isSkillsLoaded = true
        if (isCanOpenMap)
            toNext()
    }

    private fun Exception.toLog(attachment: String = "") {
        Log.e("test", "$attachment: $localizedMessage")
    }

    override fun onJobTypesLoaded(list: List<JobType>?) {
        if (list != null) {
            try {
                realm.executeTransaction {
                    it.createObject<JobTypeRealm>()
                }
            } catch (e: Exception) {
                e.toLog("init")
            }
            try {
                realm.executeTransaction {
                    it.delete<JobTypeRealm>()
                    try {
                        it.createObject<JobTypeRealm>()
                    } catch (e: Exception) {
                        e.toLog("double")
                    }
                    it.copyToRealm(list.map { it.toRealmVersion() })
                }
            } catch (e: Exception) {
                e.toLog("long")
            }
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