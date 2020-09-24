package com.byte4b.judebo.activities

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R
import com.byte4b.judebo.models.CustomAd
import com.byte4b.judebo.openBaseUrl
import com.google.gson.Gson
import kotlinx.android.synthetic.main.ad_video.*

class AdVideoActivity : AppCompatActivity(R.layout.ad_video) {

    private var ad: CustomAd? = null
    private var isCanClose = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("ad", "VideoLoadedAcivity")
        ad = Gson().fromJson(intent.getStringExtra("ad"), CustomAd::class.java)

        videoView.apply {
            setVideoURI(Uri.parse(ad?.url_source))
            requestFocus()
            start()
            setOnCompletionListener { repeat(0) {} }
        }

        cancel_icon.setOnClickListener { if (isCanClose) finish() }
        videoView.setOnClickListener { openBaseUrl(ad?.url_link ?: "") }
        Handler().postDelayed(
            {
                isCanClose = true
                cancel_icon.setImageResource(R.drawable.advertising_interstial_close_enable)
            },
            (ad?.time ?: 5) * 1000L
        )
    }
}