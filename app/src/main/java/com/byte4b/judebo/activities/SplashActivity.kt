package com.byte4b.judebo.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R
import com.byte4b.judebo.getLangFromLocale
import com.byte4b.judebo.models.*
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.delete
import java.util.*

class SplashActivity : AppCompatActivity(), ServiceListener {

    private val realm by lazy { Realm.getDefaultInstance() }

    private var isSkillsLoaded = false
    private var isJobTypesLoaded = false
    private var isAnimationEnded = false
    private var isRatesUpdated = false
    private var isSubscriptionUpdated = false
    private val isCanOpenMap get() =
        isJobTypesLoaded
                && isSkillsLoaded
                && isAnimationEnded
                && isRatesUpdated
                && isSubscriptionUpdated

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

        setting.lastOpenedFragmentName = null

        Handler().postDelayed({
            isAnimationEnded = true
            if (isCanOpenMap)
                toNext()
        }, 1000)

        ApiServiceImpl(this).apply {
            val locale = setting.getCurrentLanguage().locale
            val nowMillis = Calendar.getInstance().timeInMillis
            val lastUpdateMillis = setting.lastUpdateDynamicDataFromServer.toLong()

            if (nowMillis > lastUpdateMillis + Setting.PERIOD_UPDATE_DYNAMIC_DATA_FROM_SERVER_IN_MINUTE * 60L * 1000) {
                getSkills(locale)
                getJobTypes(locale)
                getRates(locale)
                getSubscriptions(locale)
                setting.lastUpdateDynamicDataFromServer = nowMillis.toString()
            } else {
                isSubscriptionUpdated = true
                isRatesUpdated = true
                isJobTypesLoaded = true
                isSkillsLoaded = true
            }
        }
    }

    private fun toNext() {
        startActivity<MainActivity>()
        finish()
    }

    override fun onSubscriptionsLoaded(list: List<Subscription>?) {
        if (list != null) {
            try {
                realm.executeTransaction {
                    it.createObject<SubscriptionRealm>()
                }
            } catch (e: Exception) {
                e.toLog("init")
            }
            try {
                realm.executeTransaction {
                    it.delete<SubscriptionRealm>()
                    try {
                        it.createObject<SubscriptionRealm>()
                    } catch (e: Exception) {
                        e.toLog("double")
                    }
                    it.copyToRealm(list.map { it.toRealmVersion() })
                }
            } catch (e: Exception) {
                e.toLog("long")
            }
        }

        isSubscriptionUpdated = true
        if (isCanOpenMap)
            toNext()
    }

    override fun onRatesLoaded(list: List<CurrencyRate>?) {
        if (list != null) {
            try {
                realm.executeTransaction {
                    it.createObject<CurrencyRateRealm>()
                }
            } catch (e: Exception) {
                e.toLog("init")
            }
            try {
                realm.executeTransaction {
                    it.delete<CurrencyRateRealm>()
                    try {
                        it.createObject<CurrencyRateRealm>()
                    } catch (e: Exception) {
                        e.toLog("double")
                    }
                    it.copyToRealm(list.map { it.toRealmVersion() })
                }
            } catch (e: Exception) {
                e.toLog("long")
            }
        }

        isRatesUpdated = true
        if (isCanOpenMap)
            toNext()
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
                    it.copyToRealm(list.filterNot { it.name.trim().isEmpty() }.map { it.toRealmVersion() })
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