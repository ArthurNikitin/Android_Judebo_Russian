package com.byte4b.judebo.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.*
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.LanguagesAdapter
import com.byte4b.judebo.adapters.SkillsAdapter
import com.byte4b.judebo.fragments.DetailsMapFragment
import com.byte4b.judebo.models.*
import com.byte4b.judebo.utils.Setting
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.flexbox.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_vocation_edit.*
import java.text.SimpleDateFormat
import java.util.*

class VocationEditActivity : AppCompatActivity() {

    private val realm by lazy { Realm.getDefaultInstance() }
    private var job: Vocation? = null
    private val setting by lazy { Setting(this) }
    private val skillsRealm by lazy { realm.where<SkillRealm>().findAll().map { it.toBasicVersion() } }

    companion object {
        private const val REQUEST_PICTURE = 102
        const val REQUEST_LANGUAGES = 103
        const val REQUEST_SKILLS = 104
    }

    var EditText.data: String?
        get() = text.toString()
        set(value) = setText(value)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vocation_edit)
        supportActionBar?.hide()

        val jobInfo = Gson().fromJson(intent.getStringExtra("data"), Vocation::class.java)
        job = jobInfo

        initJobTypes()
        initCurrencies()

        try {
            val currency = currencies.firstOrNull { it.id == jobInfo.UF_GROSS_CURRENCY_ID }

            if (!isRtl(this)) {
                phone_tv.setLeftDrawable(R.drawable.edit_page_phone, 50)
                email_tv.setLeftDrawable(R.drawable.edit_page_mail, 50)
            } else {
                phone_tv.setRightDrawable(R.drawable.edit_page_phone, 50)
                email_tv.setRightDrawable(R.drawable.edit_page_mail, 50)
            }

            name_tv.setText(jobInfo.NAME)

            if (!jobInfo.UF_DETAIL_IMAGE.isNullOrEmpty()) {
                Picasso.get()
                    .load(jobInfo.UF_DETAIL_IMAGE)
                    .placeholder(R.drawable.edit_page_default_logo)
                    .into(logo_iv)
            }

            try {
                salary_tv.data = jobInfo.UF_GROSS_PER_MONTH?.round()?.trim()
                val selectedCurrency = currency ?: setting.getCurrentCurrency()
                currencies.indices.forEach {
                    if (currencies[it].id == selectedCurrency.id) {
                        salaryVal_tv.setSelection(it)
                        return@forEach
                    }
                }

                salary_tv.setRightDrawable(currency?.icon ?: R.drawable.iusd)

                jobInfo.apply {
                    supportFragmentManager.beginTransaction()
                        .add(
                            R.id.containerFragment, DetailsMapFragment(
                                MyMarker(
                                    "", AUTO_TRANSLATE ?: 0,
                                    COMPANY ?: "", DETAIL_TEXT ?: "", UF_JOBS_ID ?: 0,
                                    NAME ?: "", UF_CONTACT_EMAIL ?: "",
                                    UF_CONTACT_PHONE ?: "",
                                    UF_DETAIL_IMAGE ?: "", UF_DISABLE ?: "",
                                    UF_GOLD_GROSS_MONTH ?: "",
                                    UF_GOLD_PER_MONTH ?: "",
                                    UF_GROSS_CURRENCY_ID ?: 0,
                                    UF_GROSS_PER_MONTH ?: "",
                                    UF_JOBS_ID ?: 0, UF_LANGUAGE_ID_ALL ?: "",
                                    null, UF_LOGO_IMAGE, UF_MAP_POINT ?: "",
                                    location[0], location[1], UF_MAP_RENDERED ?: 0,
                                    UF_MODIFED ?: "", UF_PREVIEW_IMAGE ?: "",
                                    UF_SKILLS_ID_ALL ?: "",
                                    UF_TYPE_OF_JOB_ID ?: 0
                                ), true
                            )
                        )
                        .commit()
                }
            } catch (e: Exception) {
            }


            setSkillsList()
            setLanguagesList()

            phone_tv.data = jobInfo.UF_CONTACT_PHONE
            email_tv.data = jobInfo.UF_CONTACT_EMAIL

            lastUpdate_tv.text = "#${jobInfo.UF_JOBS_ID}\n${jobInfo.UF_MODIFED}"
            company_tv.data = jobInfo.COMPANY ?: ""
            //jobType_tv.text = jobInfo.UF_TYPE_OF_JOB_NAME ?: ""

            details_tv.data = jobInfo.DETAIL_TEXT ?: ""
        } catch (e: Exception) {}

        name_tv.hideKeyboard()
    }

    private fun initJobTypes() {
        jobType_tv.adapter = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,
            realm.where<JobTypeRealm>().findAll().map { it.name }.filter { it.trim() != "" }
        )
    }

    private fun initCurrencies() {
        salaryVal_tv.adapter = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,
            currencies.map { it.name }
        )

        //UF_GROSS_CURRENCY_ID
    }

    fun closeClick(v: View) = finish()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICTURE -> logo_iv.setImageURI(data?.data)
            REQUEST_SKILLS -> {
                if (resultCode == Activity.RESULT_OK) {
                    job?.UF_SKILLS_ID_ALL = data?.getStringExtra("skills")
                    setSkillsList()
                }
            }
            REQUEST_LANGUAGES -> {
                if (resultCode == Activity.RESULT_OK) {
                    job?.UF_LANGUAGE_ID_ALL = data?.getStringExtra("langs")
                    setLanguagesList()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setLanguagesList() {
        if (job == null) return
        val jobInfo = job!!

        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        try {
            val languagesList = jobInfo.UF_LANGUAGE_ID_ALL?.split(",")?.map {
                languages.first { lang -> lang.id == it.toInt() }
            }
            lang_rv.layoutManager = layoutManager
            lang_rv.adapter =
                LanguagesAdapter(this,
                    (languagesList ?: listOf(setting.getCurrentLanguage())) +
                            Language(name = getString(R.string.edit_item_add_language),
                                flag = R.drawable.button_plus_gray),
                    isDetails = true, isEditor = true, vocation = jobInfo
                )
        } catch (e: Exception) {}
    }

    private fun setSkillsList() {
        if (job == null) return
        val jobInfo = job!!

        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.FLEX_START

        filters_tv.layoutManager = layoutManager
        if (jobInfo.UF_SKILLS_ID_ALL.isNullOrEmpty()) {
            filters_tv.adapter = SkillsAdapter(this,
                listOf(getString(R.string.edit_item_add_tag)),
                isDetails = true, isEditor = true, vocation = jobInfo)
        } else {
            val vocationSkillsIds = jobInfo.UF_SKILLS_ID_ALL!!
                .split(",")
                .filterNot { it == Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN }
            filters_tv.adapter = SkillsAdapter(this,
                 skillsRealm.filter { it.id.toString() in vocationSkillsIds }.map { it.name } +
                        getString(R.string.edit_item_add_tag),
                isDetails = true, isEditor = true, vocation = jobInfo)
        }
    }

    fun deleteClick(v: View) {
        AlertDialog.Builder(this)
            .setTitle(R.string.request_request_delete_title)
            .setMessage(R.string.request_request_delete_message)
            .setPositiveButton(R.string.request_request_delete_ok) { dialog, _ ->

                Realm.getDefaultInstance().executeTransaction {
                    val vocationRealm = it.where<VocationRealm>()
                        .equalTo("UF_JOBS_ID", job?.UF_JOBS_ID)
                        .findFirst()
                    vocationRealm?.apply {
                        isHided = true
                        val format = SimpleDateFormat("dd.mm.yyyy hh:mm:ss")
//17.08.2021 11:46:20
                        UF_MODIFED = format.format(Calendar.getInstance().time)
                        Log.e("test", UF_MODIFED.toString())

                        AUTO_TRANSLATE = null
                        COMPANY = null
                        DETAIL_TEXT = null
                        NAME = null
                        UF_CONTACT_EMAIL = null
                        UF_CONTACT_PHONE = null
                        UF_DETAIL_IMAGE = null
                        UF_DISABLE = null
                        UF_GOLD_GROSS_MONTH = null
                        UF_GOLD_PER_MONTH = null
                        UF_GROSS_CURRENCY_ID = null
                        UF_GROSS_PER_MONTH = null
                        UF_LANGUAGE_ID_ALL = null
                        UF_LOGO_IMAGE = null
                        UF_MAP_POINT = null
                        UF_MAP_RENDERED = null
                        UF_PREVIEW_IMAGE = null
                        UF_SKILLS_ID_ALL = null
                        UF_TYPE_OF_JOB_ID = null
                        UF_USER_ID = null
                    }
                }
                //todo: update query to server
                //todo: reload list

                dialog.dismiss()
            }
            .setNegativeButton(R.string.request_request_delete_cancel) { d, _ -> d.cancel() }
            .show()
    }

    fun saveClick(v: View) = toast("save stub")

    fun toLanguagesClick(v: View) {
        val selectIntent = Intent(this, LanguagesActivity::class.java)
        selectIntent.putExtra("data", Gson().toJson(job))
        startActivityForResult(selectIntent, REQUEST_LANGUAGES)
    }

    fun toSkillsClick(v: View) {
        val selectIntent = Intent(this, SkillsActivity::class.java)
        selectIntent.putExtra("data", Gson().toJson(job))
        startActivityForResult(selectIntent, REQUEST_SKILLS)
    }

    fun setAvatar(v: View) {
        askPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {

            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                REQUEST_PICTURE
            )
        }.onDeclined { e ->
            if (e.hasDenied()) {
                AlertDialog.Builder(this).apply {
                    setMessage("Accept permissions")
                    setPositiveButton(AlertDialog.BUTTON_POSITIVE) { dialog, _ ->
                        e.askAgain()
                        dialog.dismiss()
                    }
                }
            }

            if (e.hasForeverDenied()) {
                AlertDialog.Builder(this).apply {
                    setMessage("Permission request")
                    setPositiveButton(AlertDialog.BUTTON_POSITIVE) { dialog, _ ->
                        e.goToSettings()
                        dialog.dismiss()
                    }
                }
            }
        }
    }

}