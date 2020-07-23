package com.byte4b.judebo.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R
import com.byte4b.judebo.models.MyMarker
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        supportActionBar?.hide()

        val jobInfo = Gson().fromJson(intent.getStringExtra("marker"), MyMarker::class.java)

        name_tv.text = jobInfo.NAME

        if (jobInfo.UF_LOGO_IMAGE.isNotEmpty()) {
            Picasso.get()
                .load(jobInfo.UF_LOGO_IMAGE)
                .placeholder(R.drawable.big_logo_setting)
                .error(R.drawable.big_logo_setting)
                .into(icon_iv)
        }

    }

    fun closeClick(view: View) = finish()
}