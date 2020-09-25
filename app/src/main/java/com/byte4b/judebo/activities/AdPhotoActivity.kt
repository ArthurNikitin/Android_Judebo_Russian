package com.byte4b.judebo.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
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
        image_iv.setOnClickListener { openBaseUrl(ad?.url_link ?: "") }
        Handler().postDelayed(
            {
                isCanClose = true
                cancel_icon.setImageResource(R.drawable.advertising_interstial_close_enable)
                try {
                    Handler().postDelayed({ finish() }, ((ad?.time ?: 5) * 1000L))
                } catch (e: Exception) {}
            },
            ((ad?.time ?: 5) * 1000L)
        )
    }

    override fun onBackPressed() {}

}