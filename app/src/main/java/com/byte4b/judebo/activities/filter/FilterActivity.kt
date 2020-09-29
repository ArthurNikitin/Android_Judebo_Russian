package com.byte4b.judebo.activities.filter

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.LanguagesAdapter
import com.byte4b.judebo.adapters.SkillsAdapter
import com.byte4b.judebo.models.JobTypeRealm
import com.byte4b.judebo.models.SkillRealm
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import com.google.android.flexbox.*
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_filter.*

class FilterActivity : AppCompatActivity() {

    private val realm by lazy { Realm.getDefaultInstance() }
    private val setting by lazy { Setting(this) }
    private val skillsRealm by lazy {
        realm.where<SkillRealm>().findAll().map { it.toBasicVersion() }
    }
    private lateinit var jobsType: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        supportActionBar?.hide()

        jobsType = mutableListOf(getString(R.string.search_all_types_of_jobs))
        jobsType.addAll(
            realm.where<JobTypeRealm>().findAll().map { it.name }.filter { it.trim() != "" }
        )

        spinner.setItems(jobsType)

        spinner.setOnClickListener {
            jobType_container.setBackgroundResource(R.drawable.green_container)
        }
        spinner.setOnItemSelectedListener { _, _, _, _ ->
            jobType_container.setBackgroundResource(R.drawable.salary_container_background)
        }
        spinner.setOnNothingSelectedListener {
            jobType_container.setBackgroundResource(R.drawable.salary_container_background)
        }

        setLanguagesList()
        setSkillsList()
    }

    private fun setLanguagesList() {
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        try {
            val languagesList = setting.filterLanguagesIds.map {
                languages.first { lang -> lang.id == it.toInt() }
            }
            lang_rv.layoutManager = layoutManager
            lang_rv.adapter =
                LanguagesAdapter(this, languagesList,
                    isDetails = true, isEditor = true, vocation = Vocation())
        } catch (e: Exception) {}
    }

    private fun setSkillsList() {
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.FLEX_START

        filters_tv.layoutManager = layoutManager
        if (setting.filterSkillsIds.isNullOrEmpty()) {
            filters_tv.adapter = SkillsAdapter(
                this,
                listOf(),
                isDetails = true, isEditor = true, vocation = Vocation()
            )
        } else {
            val vocationSkillsIds = setting.filterSkillsIds
                .filterNot { it == Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN }
            filters_tv.adapter = SkillsAdapter(this,
                skillsRealm.filter { it.id.toString() in vocationSkillsIds }.map { it.name },
                isDetails = true, isEditor = true, vocation = Vocation()
            )
        }
    }

    override fun onRestart() {
        super.onRestart()
        setLanguagesList()
        setSkillsList()
    }

    fun closeClick(v: View) = finish()

    fun saveClick(v: View) {
        setting.isFilterActive = true
        val type = jobsType[spinner.selectedIndex]
        setting.filterJobType = if (type == getString(R.string.search_all_types_of_jobs)) "" else type
    }

    fun toLanguagesClick(v: View) = startActivity<FilterLanguagesActivity>()

    fun toSkillsClick(v: View) = startActivity<FilterSkillsActivity>()

    fun clearFilter(v: View) {
        setting.filterSkillsIds = listOf()
        setting.filterLanguagesIds = listOf()
        setting.isFilterActive = false
        setting.filterJobType = ""//jobsType[spinner.selectedIndex]
    }

}