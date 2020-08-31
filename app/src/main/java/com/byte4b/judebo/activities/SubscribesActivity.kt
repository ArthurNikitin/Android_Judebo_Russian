package com.byte4b.judebo.activities

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R

class SubscribesActivity : AppCompatActivity(R.layout.activity_subscribes) {

    fun closeClick(v: View) = finish()

    override fun onStart() {
        super.onStart()
        supportActionBar?.hide()
    }

}