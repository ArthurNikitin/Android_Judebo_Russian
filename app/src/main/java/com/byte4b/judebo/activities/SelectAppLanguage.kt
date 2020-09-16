package com.byte4b.judebo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.SelectOneLanguageAdapter
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.utils.Setting
import kotlinx.android.synthetic.main.setting_select.*

class SelectAppLanguage : AppCompatActivity() {

    private val setting by lazy { Setting(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_select)
        supportActionBar?.hide()
        title1.text = getString(R.string.settings_title_current_language)
        try {
            selected_rv.layoutManager = LinearLayoutManager(this)
            initData()
        } catch (e: Exception) {}
    }

    private fun initData() {
        try {
            selected_rv.adapter =
                SelectOneLanguageAdapter(this, languages, setting.getCurrentLanguage().id)
        } catch (e: Exception) {}
    }

    fun saveClick(langId: Int) {
        val intentRes = Intent()
        intentRes.putExtra("langs", langId)
        setResult(Activity.RESULT_OK, intentRes)
        finish()
    }

    fun closeClick(v: View) {
        val intentRes = Intent()
        setResult(Activity.RESULT_CANCELED, intentRes)
        finish()
    }

    override fun onBackPressed() {
        val intentRes = Intent()
        setResult(Activity.RESULT_CANCELED, intentRes)
        finish()
        super.onBackPressed()
    }

}