package com.byte4b.judebo.activities.filter

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.FilterLanguagesAdapter
import com.byte4b.judebo.models.Language
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.utils.Setting
import kotlinx.android.synthetic.main.activity_languages_activitiy.*

class FilterLanguagesActivity : AppCompatActivity() {

    private var selectedLanguages = mutableListOf<Language>()
    private val setting by lazy { Setting(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_languages)
        supportActionBar?.hide()

        selectedLanguages.addAll(setting.filterLanguagesIds.map { id ->
            languages.first { id == it.id.toString() }
        })

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
        setting.filterLanguagesIds = selectedLanguages.map { it.id.toString() }
        finish()
    }

    fun closeClick(v: View) {
        finish()
    }

}