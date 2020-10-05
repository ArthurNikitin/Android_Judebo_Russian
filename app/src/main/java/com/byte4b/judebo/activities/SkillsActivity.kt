package com.byte4b.judebo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.EditSkillsAdapter
import com.byte4b.judebo.hideKeyboard
import com.byte4b.judebo.models.AuthResult
import com.byte4b.judebo.models.Skill
import com.byte4b.judebo.models.SkillRealm
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.setLeftDrawable
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.delete
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_skills.*

class SkillsActivity : AppCompatActivity(), ServiceListener {

    private val setting by lazy { Setting(this) }
    private val realm by lazy { Realm.getDefaultInstance() }
    lateinit var vocation: Vocation
    private val skills get() = realm.where<SkillRealm>().findAll()
        .map { it.toBasicVersion() }
        .filterNot { it.id == Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN.toInt() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skills)
        supportActionBar?.hide()

        selected_rv.layoutManager = LinearLayoutManager(this)
        notSelected_rv.layoutManager = LinearLayoutManager(this)

        vocation = Gson().fromJson(intent.getStringExtra("data"), Vocation::class.java)
        initData()

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

        refresher.setOnRefreshListener { refresher.isRefreshing = false }
    }

    private fun filterOff() = filters_tv.setText("")

    private fun initData() {
        val vocationSkills = vocation.UF_SKILLS_ID_ALL.splitToArray()
        val lists = skills.partition { it.id.toString() in vocationSkills }
        selected_rv.adapter = EditSkillsAdapter(
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

        notSelected_rv.adapter = EditSkillsAdapter(this, notSelectedList, false)

        if (notSelectedList.isEmpty())
            add_new_iv.visibility = View.VISIBLE
        else
            add_new_iv.visibility = View.GONE
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
        filterOff()
        hideKeyboard()
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
        return split(",")
            .map { it.trim() }
            .filterNot { it.isEmpty() }
            .filterNot { it == "284" }
            .filterNot { it == Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN }
            .toMutableList()
    }

    private var addingSkill = ""
    fun createNewSkill(v: View) {
        addingSkill = filters_tv.text.toString()
        add_new_iv.isEnabled = false
        ApiServiceImpl(this).createSkill(
            setting.getCurrentLanguage().locale,
            login = setting.email ?: "",
            token = setting.token ?: "",
            name = addingSkill
        )
        try { refresher.isRefreshing = true } catch (e: Exception) {}
    }

    private fun stopAnimation() = try { refresher.isRefreshing = false } catch (e: Exception) {}

    override fun onSkillCreated(result: AuthResult?) {
        if (result?.status == "success") {
            Handler().postDelayed({
                ApiServiceImpl(this).getSkills(setting.getCurrentLanguage().locale)
            }, Setting.TAGS_SLEEP_AFTER_SAVE_IN_SECONDS * 1000)
        } else if (result != null) {
            Toasty.error(this, result.data).show()
            stopAnimation()
            add_new_iv.isEnabled = true
        } else {
            Toasty.error(this, R.string.error_no_internet).show()
            stopAnimation()
            add_new_iv.isEnabled = true
        }
    }

    override fun onSkillsLoaded(list: List<Skill>?) {
        add_new_iv.isEnabled = true
        stopAnimation()

        if (list != null) {
            try {
                realm.executeTransaction {
                    it.createObject<SkillRealm>()
                }
            } catch (e: Exception) {}
            try {
                realm.executeTransaction {
                    it.delete<SkillRealm>()
                    try {
                        it.createObject<SkillRealm>()
                    } catch (e: Exception) {}
                    it.copyToRealm(
                        list
                            .filterNot { it.name.trim().isEmpty() }
                            .map { it.toRealmVersion() }
                    )
                }
            } catch (e: Exception) {}
        }

        val skill = realm.where<SkillRealm>().equalTo("name", addingSkill).findFirst()
        if (skill != null)
            addSkill(skill.id)
    }

}