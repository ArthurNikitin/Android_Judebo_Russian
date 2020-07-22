package com.byte4b.judebo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        textView2.text = intent.getStringExtra("marker")
    }
}