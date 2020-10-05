package com.byte4b.judebo

import android.app.Application
import android.content.Context
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Handler
import androidx.multidex.MultiDex
import com.byte4b.judebo.activities.AdBannerActivity
import com.byte4b.judebo.activities.AdPhotoActivity
import com.byte4b.judebo.activities.AdVideoActivity
import com.byte4b.judebo.models.*
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.delete
import java.util.*


class App : Application(), ServiceListener {

    private val setting by lazy { Setting(this) }
    private val realm by lazy { Realm.getDefaultInstance() }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        try {
            Realm.init(this)
        } catch (e: Exception) {}

        MobileAds.initialize(this) {}
        val handler = Handler {
            if (setting.isLastTryShowAdHaveError) {
                if (Calendar.getInstance().timestamp >
                    setting.lastAdShowTimeStamp + Setting.APP_CRON_FREQUENCY_IN_SECONDS
                ) {
                    ApiServiceImpl(this).loadAd(setting.getCurrentLanguage().locale)
                }
                setting.isLastTryShowAdHaveError = false
            } else {
                if (Calendar.getInstance().timestamp >
                    setting.lastAdShowTimeStamp + Setting.JSON_REQUEST_ADV_PERIOD_IN_SECONDS
                ) {
                    ApiServiceImpl(this).loadAd(setting.getCurrentLanguage().locale)
                }
            }
            ApiServiceImpl(this).apply {
                val locale = setting.getCurrentLanguage().locale
                val nowMillis = Calendar.getInstance().timeInMillis
                val lastUpdateMillis = setting.lastUpdateDynamicDataFromServer.toLong()

                if (nowMillis > lastUpdateMillis +
                    Setting.PERIOD_UPDATE_DYNAMIC_DATA_FROM_SERVER_IN_MINUTE * 60L * 1000) {

                    getSkills(locale)
                    getJobTypes(locale)
                    getRates(locale)
                    getSubscriptions(locale)
                    setting.lastUpdateDynamicDataFromServer = nowMillis.toString()
                }
            }

            true
        }

        Thread {
            while (true) {
                Thread.sleep(Setting.APP_CRON_FREQUENCY_IN_SECONDS * 1000L)
                handler.sendEmptyMessage(0)
            }
        }.start()
    }

    override fun onAdLoaded(result: CustomAd?) {
        if (result != null && !result.isEmpty) {
            setting.lastAdShowTimeStamp = Calendar.getInstance().timestamp
            if (result.isPhoto)
                startActivity<AdPhotoActivity> {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                    putExtra("ad", Gson().toJson(result))
                }
            else if (result.isVideo)
                startActivity<AdVideoActivity> {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                    putExtra("ad", Gson().toJson(result))
                }

        } else if (setting.maxVocations == Setting.LIMIT_VACANCIES_WITHOUT_SUBSCRIPTION) {
            if (Setting.ADV_GOOGLE_ADV_ADMOB_TYPE == "fullscreen") {
                InterstitialAd(this).apply {
                    adUnitId = "ca-app-pub-5400099956888878/3325823769"
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            setting.lastAdShowTimeStamp = Calendar.getInstance().timestamp
                            show()
                        }

                        override fun onAdFailedToLoad(p0: Int) {
                            super.onAdFailedToLoad(p0)
                            setting.isLastTryShowAdHaveError = false
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            } else if (Setting.ADV_GOOGLE_ADV_ADMOB_TYPE == "banner") {
                startActivity<AdBannerActivity> {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                }
            }
        } else {
            setting.isLastTryShowAdHaveError = true
        }
    }

    override fun onSubscriptionsLoaded(list: List<Subscription>?) {
        if (list != null) {
            try {
                realm.executeTransaction {
                    it.createObject<SubscriptionRealm>()
                }
            } catch (e: Exception) {}
            try {
                realm.executeTransaction {
                    it.delete<SubscriptionRealm>()
                    try {
                        it.createObject<SubscriptionRealm>()
                    } catch (e: Exception) {}
                    it.copyToRealm(list.map { it.toRealmVersion() })
                }
            } catch (e: Exception) {}
        }
    }

    override fun onRatesLoaded(list: List<CurrencyRate>?) {
        if (list != null) {
            try {
                realm.executeTransaction {
                    it.createObject<CurrencyRateRealm>()
                }
            } catch (e: Exception) {}
            try {
                realm.executeTransaction {
                    it.delete<CurrencyRateRealm>()
                    try {
                        it.createObject<CurrencyRateRealm>()
                    } catch (e: Exception) {}
                    it.copyToRealm(list.map { it.toRealmVersion() })
                }
            } catch (e: Exception) {}
        }
    }

    override fun onSkillsLoaded(list: List<Skill>?) {
        if (list != null) {
            try {
                realm.executeTransaction {
                    it.createObject<SkillRealm>()
                }
            } catch (e: Exception) {}
            try {
                realm.executeTransaction {
                    it.delete<SkillRealm>()
                    try {
                        it.createObject<SkillRealm>()
                    } catch (e: Exception) {}
                    it.copyToRealm(list.filterNot { it.name.trim().isEmpty() }.map { it.toRealmVersion() })
                }
            } catch (e: Exception) {}
        }
    }

    override fun onJobTypesLoaded(list: List<JobType>?) {
        if (list != null) {
            try {
                realm.executeTransaction {
                    it.createObject<JobTypeRealm>()
                }
            } catch (e: Exception) {}
            try {
                realm.executeTransaction {
                    it.delete<JobTypeRealm>()
                    try {
                        it.createObject<JobTypeRealm>()
                    } catch (e: Exception) {}
                    it.copyToRealm(list.map { it.toRealmVersion() })
                }
            } catch (e: Exception) {}
        }
    }


}