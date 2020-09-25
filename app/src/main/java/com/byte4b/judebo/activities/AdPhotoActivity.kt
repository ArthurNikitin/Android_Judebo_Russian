package com.byte4b.judebo.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.byte4b.judebo.R
import com.byte4b.judebo.models.CustomAd
import com.byte4b.judebo.openBaseUrl
import com.google.gson.Gson
import kotlinx.android.synthetic.main.ad_photo.*

class AdPhotoActivity : AppCompatActivity(R.layout.ad_photo) {

    private var ad: CustomAd? = null
    private var isCanClose = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        Log.e("ad", "PhotoLoadedAcivity")
        ad = Gson().fromJson(intent.getStringExtra("ad"), CustomAd::class.java)

        if (ad?.url_source != null)
            Glide.with(this)
                .load(ad!!.url_source!!)
                .into(image_iv)
        cancel_icon.setOnClickListener { if (isCanClose) finish() }
        image_iv.setOnClickListener {
            if (!isCanClose)
                openBaseUrl(ad?.url_link ?: "")
            else
                finish()
        }

        var time = 0
        val period = (ad?.time ?: 5) * 1000L
        progressBar.max = ad?.time ?: 5
        val handler = Handler {
            time++
            progressBar.setDonut_progress(time.toString())
            timerView.text = (period/1000 - time).toString()

            if (time.toLong() == period/1000) {
                isCanClose = true
                timerView.visibility = View.GONE
                progressBar.visibility = View.GONE
                cancel_icon.alpha = 1f
                cancel_icon.setImageResource(R.drawable.advertising_interstial_close_enable)
            }
            if (time.toLong() == period/1000 * 3)
                finish()
            true
        }

        Thread {
            while (time <= period * 3) {
                handler.sendEmptyMessage(0)
                Thread.sleep(1000)
            }
        }.start()
    }

    override fun onBackPressed() { if (isCanClose) finish() }

    fun close(v: View) {
        if (isCanClose) finish()
    }

}