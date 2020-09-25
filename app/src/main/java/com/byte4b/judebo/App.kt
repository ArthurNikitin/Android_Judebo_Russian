package com.byte4b.judebo

import android.app.Application
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Handler
import android.util.Log
import com.byte4b.judebo.activities.AdPhotoActivity
import com.byte4b.judebo.activities.AdVideoActivity
import com.byte4b.judebo.models.CustomAd
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.google.android.gms.ads.*
import com.google.gson.Gson
import java.util.*


class App : Application(), ServiceListener {

    private val setting by lazy { Setting(this) }

    override fun onCreate() {
        super.onCreate()

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
        Log.e("ad", "onAdLoaded")
        Log.e("ad", Gson().toJson(result))
        if (result != null && !result.isEmpty) {
            Log.e("ad", "custom ads")
            setting.lastAdShowTimeStamp = Calendar.getInstance().timestamp
            Log.e("ad", "photo = ${result.isPhoto}, video = ${result.isVideo}")
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
            Log.e("ad", "google ads")
            MobileAds.initialize(this) {}
            val mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = "ca-app-pub-5400099956888878/3325823769"
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    setting.lastAdShowTimeStamp = Calendar.getInstance().timestamp
                    mInterstitialAd.show()
                }

                override fun onAdFailedToLoad(p0: Int) {
                    super.onAdFailedToLoad(p0)
                    setting.isLastTryShowAdHaveError = false
                }
            }
            val rc = RequestConfiguration.Builder()
            rc.setTestDeviceIds(mutableListOf("7B95E5B1FF96A171FD6EFFBBC0FB7518"))
            MobileAds.setRequestConfiguration(rc.build())
            mInterstitialAd.loadAd(AdRequest.Builder().build())
        } else {
            Log.e("ad", "error load")
            setting.isLastTryShowAdHaveError = true
        }
    }

}