package com.byte4b.judebo.activities.filter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.FilterSkillsAdapter
import com.byte4b.judebo.models.Skill
import com.byte4b.judebo.models.SkillRealm
import com.byte4b.judebo.rtlSupportActivation
import com.byte4b.judebo.setLeftDrawable
import com.byte4b.judebo.utils.Setting
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_filter_skills.*

class FilterSkillsActivity : AppCompatActivity() {

    private val skills get() = realm.where<SkillRealm>().findAll().map { it.toBasicVersion() }
    private val realm by lazy { Realm.getDefaultInstance() }
    private var selectedSkills = mutableListOf<Skill>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rtlSupportActivation()
        setContentView(R.layout.activity_filter_skills)
        supportActionBar?.hide()

        try {
            Realm.init(this)
        } catch (e: Exception) {}

        try {
            selectedSkills.addAll(
                intent!!.getStringExtra("data")!!.split(",").filter { !it.isEmpty() }.map { id ->
                    skills.first { id == it.id.toString() }
                })
        } catch (e: Exception) {}

        try {
            selected_rv.layoutManager = LinearLayoutManager(this)
            notSelected_rv.layoutManager = LinearLayoutManager(this)

            filters_tv.setLeftDrawable(R.drawable.jobs_list_filter)
            filters_tv.doOnTextChanged { text, _, _, _ ->
                val txt = text.toString()
                initData()

                if (txt.trim().isEmpty()) {
                    closeFilter_iv.visibility = View.GONE
                    filters_tv.setPadding(0, 0, 0, 0)
                } else {
                    closeFilter_iv.visibility = View.VISIBLE
                    filters_tv.setPadding(0, 0, 15, 0)
                }
            }
            closeFilter_iv.setOnClickListener { filterOff() }

            initData()
        } catch (e: Exception) {}
    }

    private fun filterOff() = filters_tv.setText("")

    private fun initData() {
        try {
            val vocationSkills = selectedSkills.map { it.id }
            val lists = skills.partition { it.id in vocationSkills }
            selected_rv.adapter = FilterSkillsAdapter(
                this,
                lists.first.filterNot { it.name.trim().isEmpty() },
                true
            )

            var notSelectedList = lists.second
                .filterNot { it.name.trim().isEmpty() }.reversed()
                .filter { it.name.contains(filters_tv.text.toString(), ignoreCase = true) }

            if (filters_tv.text.toString().isEmpty())
                notSelectedList = notSelectedList
                    .filter { it.popularity?:0 > Setting.TAGS_POPULARITY_MINIMUM }

            notSelected_rv.adapter = FilterSkillsAdapter(
                this, notSelectedList, false
            )

        } catch (e: Exception) {}
    }

    fun deleteSkill(id: Int) {
        selectedSkills = selectedSkills.filter { it.id != id }.toMutableList()
        initData()
    }

    fun addSkill(id: Int) {
        selectedSkills.add(skills.first { it.id == id })
        initData()
    }

    fun saveClick(v: View) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("skills", selectedSkills.map { it.id }.joinToString(","))
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