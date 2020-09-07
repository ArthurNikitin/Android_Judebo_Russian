package com.byte4b.judebo.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R
import kotlinx.android.synthetic.main.activity_policy.*

class PolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_policy)
        supportActionBar?.hide()

        webView.loadUrl(intent.getStringExtra("url") ?: "")
    }

    fun closeClick(v: View) = finish()
}