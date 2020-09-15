package com.byte4b.judebo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.EditLanguagesAdapter
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.models.languages
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_languages_activitiy.*

class LanguagesActivity : AppCompatActivity() {

    lateinit var vocation: Vocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_languages_activitiy)
        supportActionBar?.hide()

        try {
            selected_rv.layoutManager = LinearLayoutManager(this)

            vocation = Gson().fromJson(intent.getStringExtra("data"), Vocation::class.java)
            initData()
        } catch (e: Exception) {
            Log.e("test1", e.localizedMessage?: "error")
        }
    }

    private fun initData() {
        try {
            val vocationLanguages = vocation.UF_LANGUAGE_ID_ALL.splitToArray()
            selected_rv.adapter = EditLanguagesAdapter(this,
                languages.map { Pair(it, it.id.toString() in vocationLanguages) }
            )
        } catch (e: Exception) {}
    }

    fun deleteSkill(id: Int) {
        val list = vocation.UF_LANGUAGE_ID_ALL.splitToArray()
        list.remove(id.toString())
        vocation.UF_LANGUAGE_ID_ALL = list.joinToString(",")
        initData()
    }

    fun addSkill(id: Int) {
        val list = vocation.UF_LANGUAGE_ID_ALL.splitToArray()
        list.add(id.toString())
        vocation.UF_LANGUAGE_ID_ALL = list.joinToString(",")
        initData()
    }

    fun saveClick(v: View) {
        val intentRes = Intent()
        intentRes.putExtra("langs", vocation.UF_LANGUAGE_ID_ALL)
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

    private fun String?.splitToArray(): MutableList<String> {
        if (this == null) return mutableListOf()
        return split(",")
            .map { it.trim() }
            .filterNot { it.isEmpty() }
            .toMutableList()
    }

}