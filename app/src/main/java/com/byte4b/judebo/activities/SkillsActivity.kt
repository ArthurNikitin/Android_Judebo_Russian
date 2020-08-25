package com.byte4b.judebo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.EditSkillsAdapter
import com.byte4b.judebo.models.SkillRealm
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.utils.Setting
import com.google.gson.Gson
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_skills.*

class SkillsActivity : AppCompatActivity() {

    private val realm by lazy { Realm.getDefaultInstance() }
    lateinit var vocation: Vocation
    private val skills by lazy { realm.where<SkillRealm>().findAll().map { it.toBasicVersion() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skills)
        supportActionBar?.hide()

        selected_rv.layoutManager = LinearLayoutManager(this)
        notSelected_rv.layoutManager = LinearLayoutManager(this)

        vocation = Gson().fromJson(intent.getStringExtra("data"), Vocation::class.java)
        initData()
    }

    private fun initData() {
        val vocationSkills = vocation.UF_SKILLS_ID_ALL.splitToArray()
        val lists = skills.partition { it.id.toString() in vocationSkills }
        selected_rv.adapter = EditSkillsAdapter(this, lists.first, true)
        notSelected_rv.adapter = EditSkillsAdapter(this, lists.second, false)
    }

    fun deleteSkill(id: Int) {
        val list = vocation.UF_SKILLS_ID_ALL.splitToArray()
        list.remove(id.toString())
        vocation.UF_SKILLS_ID_ALL = list.joinToString(",")
        initData()
    }

    fun addSkill(id: Int) {
        val list = vocation.UF_SKILLS_ID_ALL.splitToArray()
        list.add(id.toString())
        vocation.UF_SKILLS_ID_ALL = list.joinToString(",")
        initData()
    }

    fun saveClick(v: View) {
        val intentRes = Intent()
        intentRes.putExtra("skills", vocation.UF_SKILLS_ID_ALL)
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
        return this.split(",")
            .map { it.trim() }
            .filterNot { it.isEmpty() }
            .filterNot { it == Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN }
            .toMutableList()
    }

}