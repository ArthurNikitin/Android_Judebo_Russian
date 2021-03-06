package com.byte4b.judebo.activities.filter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.appyvet.materialrangebar.RangeBar
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.LanguagesAdapter
import com.byte4b.judebo.adapters.SkillsAdapter
import com.byte4b.judebo.models.*
import com.byte4b.judebo.rtlSupportActivation
import com.byte4b.judebo.utils.Setting
import com.google.android.flexbox.*
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_filter.*

class FilterActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_LANGUAGES_SELECT = 101
        private const val REQUEST_SKILLS_SELECT = 102
    }

    private val currentCurrency by lazy { setting.getCurrentCurrency() }
    private var filterLangs = listOf<String>()
    private var filterSkills = listOf<String>()
    private val realm by lazy { Realm.getDefaultInstance() }
    private val setting by lazy { Setting(this) }
    private val skillsRealm by lazy {
        realm.where<SkillRealm>().findAll().map { it.toBasicVersion() }
    }
    private lateinit var jobsType: MutableList<String>

    private fun showSelectedRange() {
        salary_range.apply {
            val leftValue = leftIndex.toDouble() * Setting.DEFAULT_MAX.toDouble()
            val rightValue = rightIndex.toDouble() * Setting.DEFAULT_MAX.toDouble()
            minRange_tv.text =
                (leftValue * currentCurrency.rate / 1_000_000).roundToBig().toInt().toString()
            maxRange_tv.text =
                if (rightIndex == Setting.SEARCH_GROSS_STEPS) "∞"
                else (rightValue * currentCurrency.rate / 1_000_000).roundToSmall().toInt()
                    .toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rtlSupportActivation()
        setContentView(R.layout.activity_filter)
        supportActionBar?.hide()

        currentCurrency.rate =
            realm.where<CurrencyRateRealm>()
                .equalTo("id", currentCurrency.id)
                .findFirst()
                ?.rate
                ?: currencies.firstOrNull { it.id == currentCurrency.id }?.rate
                        ?: currencies.first().rate

        jobsType = mutableListOf(getString(R.string.search_all_types_of_jobs))
        jobsType.addAll(
            realm.where<JobTypeRealm>().findAll().map { it.name }.filter { it.trim() != "" }
        )

        salary_iv.setImageResource(currentCurrency.icon)
        salary_tv.text = currentCurrency.name

        salary_range.apply {
            tickStart = 0f
            tickEnd = Setting.SEARCH_GROSS_GOLD_MAX.toFloat()
            setTickInterval(Setting.DEFAULT_MAX.toFloat())

            if (setting.filterSalary != "") {
                val (leftData, rightData) = setting.filterSalary.split("-").map { it.toInt() }
                salary_range.setRangePinsByIndices(leftData, rightData)
                showSelectedRange()
            }


            setOnRangeBarChangeListener(object : RangeBar.OnRangeBarChangeListener {
                override fun onRangeChangeListener(
                    rangeBar: RangeBar?,
                    leftPinIndex: Int,
                    rightPinIndex: Int,
                    leftPinValue: String?,
                    rightPinValue: String?
                ) {
                    showSelectedRange()
                }

                override fun onTouchStarted(rangeBar: RangeBar?) {}

                override fun onTouchEnded(rangeBar: RangeBar?) {}

            })
        }

        spinner.apply {
            setItems(jobsType)
            setOnClickListener {
                jobType_container.setBackgroundResource(R.drawable.green_container)
            }
            setOnItemSelectedListener { _, _, _, _ ->
                jobType_container.setBackgroundResource(R.drawable.salary_container_background)
            }
            setOnNothingSelectedListener {
                jobType_container.setBackgroundResource(R.drawable.salary_container_background)
            }
            val settingJobType = setting.filterJobType
            selectedIndex = jobsType.indices.firstOrNull { jobsType[it] == settingJobType } ?: 0
        }
        filterLangs = setting.filterLanguagesIds
        filterSkills = setting.filterSkillsIds

        setLanguagesList()
        setSkillsList()
    }

    private fun Double.roundToSmall(): Double {
        return ((this / 100).toInt() * 100.0)
    }

    private fun Double.roundToBig(): Double {
        return if (this == .0) .0 else (((this / 100).toInt()+1) * 100.0)
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
            val languagesList = filterLangs.filter { it.isNotEmpty() }.map {
                languages.first { lang -> lang.id == it.toInt() }
            }
            lang_rv.layoutManager = layoutManager
            lang_rv.adapter =
                LanguagesAdapter(
                    this, languagesList,
                    isDetails = true, isEditor = true, vocation = Vocation()
                )
        } catch (e: Exception) {
        }
    }

    private fun setSkillsList() {
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.FLEX_START

        filters_tv.layoutManager = layoutManager
        if (filterSkills.isNullOrEmpty()) {
            filters_tv.adapter = SkillsAdapter(
                this,
                listOf(),
                isDetails = true, isEditor = true, vocation = Vocation()
            )
        } else {
            val vocationSkillsIds = filterSkills
                .filterNot { it == Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN }
            filters_tv.adapter = SkillsAdapter(this,
                skillsRealm.filter { it.id.toString() in vocationSkillsIds }.map { it.name },
                isDetails = true, isEditor = true, vocation = Vocation()
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_SKILLS_SELECT -> {
                    filterSkills =
                        data!!.getStringExtra("skills")!!.split(",")
                    setSkillsList()
                }

                REQUEST_LANGUAGES_SELECT -> {
                    filterLangs =
                        data!!.getStringExtra("languages")!!.split(",")
                    setLanguagesList()
                }
            }
        }
    }

    fun closeClick(v: View) {
        setResult(RESULT_FIRST_USER)
        finish()
    }

    override fun onBackPressed() {
        setResult(RESULT_FIRST_USER)
        super.onBackPressed()
    }

    fun saveClick(v: View) {
        setting.isFilterActive = true
        val type = jobsType[spinner.selectedIndex]
        setting.filterJobType =
            if (type == getString(R.string.search_all_types_of_jobs)) "" else type
        setting.filterLanguagesIds = filterLangs
        setting.filterSkillsIds = filterSkills
        setting.filterSalary = "${salary_range.leftIndex}-${salary_range.rightIndex}"

        Log.e("test", setting.filterSalary)
        Log.e("test", Setting.DEFAULT_FILTER_RANGE_PARAMS)

        if (setting.filterJobType == ""
            && (setting.filterSalary == Setting.DEFAULT_FILTER_RANGE_PARAMS || setting.filterSalary == "")
            && setting.filterLanguagesIds.isEmpty()
            && setting.filterSkillsIds.isEmpty()) {
            setResult(Activity.RESULT_CANCELED)
        } else {
            setResult(Activity.RESULT_OK)
        }

        finish()
    }

    fun toLanguagesClick(v: View) = startActivityForResult(
        Intent(this, FilterLanguagesActivity::class.java).apply {
            putExtra("data", filterLangs.joinToString(","))
        },
        REQUEST_LANGUAGES_SELECT
    )

    fun toSkillsClick(v: View) = startActivityForResult(
        Intent(this, FilterSkillsActivity::class.java).apply {
            putExtra("data", filterSkills.joinToString(","))
        },
        REQUEST_SKILLS_SELECT
    )

    fun clearFilter(v: View) {
        setting.filterSkillsIds = listOf()
        setting.filterLanguagesIds = listOf()
        setting.isFilterActive = false
        setting.filterJobType = ""
        setting.filterSalary = Setting.DEFAULT_FILTER_RANGE_PARAMS
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

}