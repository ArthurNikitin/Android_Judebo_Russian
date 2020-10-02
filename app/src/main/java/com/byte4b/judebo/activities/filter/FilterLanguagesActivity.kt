package com.byte4b.judebo.activities.filter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.FilterLanguagesAdapter
import com.byte4b.judebo.models.Language
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.rtlSupportActivation
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_languages_activitiy.*

class FilterLanguagesActivity : AppCompatActivity() {

    private var selectedLanguages = mutableListOf<Language>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rtlSupportActivation()
        setContentView(R.layout.activity_filter_languages)
        supportActionBar?.hide()

        try {
            Realm.init(this)
        } catch (e: Exception) {}

        try {
            selectedLanguages.addAll(
                intent!!.getStringExtra("data")!!.split(",").filter { !it.isEmpty() }.map { id ->
                    languages.first { id == it.id.toString() }
                })
        } catch (e: Exception) {
        }

        try {
            selected_rv.layoutManager = LinearLayoutManager(this)
            initData()
        } catch (e: Exception) {}
    }

    private fun initData() {
        try {
            selected_rv.adapter = FilterLanguagesAdapter(this,
                languages.map { Pair(it, it.id in selectedLanguages.map { lang -> lang.id }) }
            )
        } catch (e: Exception) {}
    }

    fun deleteSkill(id: Int) {
        selectedLanguages = selectedLanguages.filter { it.id != id }.toMutableList()
        initData()
    }

    fun addSkill(id: Int) {
        selectedLanguages.add(languages.first { it.id == id })
        initData()
    }

    fun saveClick(v: View) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("languages", selectedLanguages.map { it.id }.joinToString(","))
        })
        finish()
    }

    fun closeClick(v: View) {
        onBackPressed()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, Intent())
        finish()
        super.onBackPressed()
    }

}