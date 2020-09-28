package com.byte4b.judebo

import android.app.Application
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Handler
import com.byte4b.judebo.activities.AdBannerActivity
import com.byte4b.judebo.activities.AdPhotoActivity
import com.byte4b.judebo.activities.AdVideoActivity
import com.byte4b.judebo.models.CustomAd
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import java.util.*


class App : Application(), ServiceListener {

    private val setting by lazy { Setting(this) }

    override fun onCreate() {
        super.onCreate()
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

        } else if (setting.maxVocations != Setting.LIMIT_VACANCIES_WITHOUT_SUBSCRIPTION) {
            startActivity<AdBannerActivity> {
                addFlags(FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            setting.isLastTryShowAdHaveError = true
        }
    }

}