package com.byte4b.judebo.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R
import com.byte4b.judebo.utils.Setting
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import kotlinx.android.synthetic.main.ad_banner.*
import kotlinx.android.synthetic.main.ad_photo.cancel_icon
import kotlinx.android.synthetic.main.ad_photo.progressBar
import kotlinx.android.synthetic.main.ad_photo.timerView

class AdBannerActivity : AppCompatActivity(R.layout.ad_banner) {

    private val setting by lazy { Setting(this) }
    private var isCanClose = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        Log.e("ad", "BannerAcivity")
        try {

            var isLoaded = false
            adView.adListener = object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError?) {
                    super.onAdFailedToLoad(p0)
                    Log.e("ad", "fail load google ad")
                    setting.isLastTryShowAdHaveError = true
                    finish()
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.e("ad", "success load google ad")
                    isLoaded = true
                }
            }

            Handler().postDelayed(
                {
                    if (!isLoaded) {
                        setting.isLastTryShowAdHaveError = true
                        finish()
                    }
                }, Setting.ADV_DEFAULT_SHOW_ADV_IN_SECONDS * 1000L
            )

            adView.loadAd(AdRequest.Builder().build())

            var time = 0
            val period = Setting.ADV_DEFAULT_SHOW_ADV_IN_SECONDS * 1000L
            progressBar.max = Setting.ADV_DEFAULT_SHOW_ADV_IN_SECONDS
            val handler = Handler {
                time++
                progressBar.setDonut_progress(time.toString())
                timerView.text = (period / 1000 - time).toString()

                if (time.toLong() == period / 1000) {
                    isCanClose = true
                    timerView.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    cancel_icon.alpha = 1f
                    cancel_icon.setImageResource(R.drawable.advertising_interstial_close_enable)
                }
                if (time.toLong() == period / 1000 * 3)
                    finish()
                true
            }

            Thread {
                while (time <= period * 3) {
                    handler.sendEmptyMessage(0)
                    Thread.sleep(1000)
                }
            }.start()
        } catch (e: Exception) {}
    }

    override fun onBackPressed() { if (isCanClose) finish() }

    fun close(v: View) {
        if (isCanClose) finish()
    }
}