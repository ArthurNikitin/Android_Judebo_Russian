package com.byte4b.judebo.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R
import com.byte4b.judebo.models.languages
import kotlinx.android.synthetic.main.activity_select.*

class SelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        //actionBar?.hide()//supportActionBar?.hide()

        listView.adapter = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,
            languages.map { it.name }
        )
    }
}