package com.byte4b.judebo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R

class LanguagesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_languages_activitiy)
        supportActionBar?.hide()
    }
}