package com.byte4b.judebo.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.byte4b.judebo.*
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.LanguagesAdapter
import com.byte4b.judebo.fragments.DetailsMapFragment
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.utils.Setting
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.flexbox.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_vocation_edit.*

class VocationEditActivity : AppCompatActivity() {

    private var job: Vocation? = null
    private val setting by lazy { Setting(this) }

    companion object {
        private const val REQUEST_PICTURE = 102
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

        logo_iv.setOnClickListener {
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
            } else {
                logo_iv.visibility = View.GONE
            }

            try {
                salary_tv.data = jobInfo.UF_GROSS_PER_MONTH?.round()?.trim()
                salaryVal_tv.text = " ${currency?.name ?: ""}"
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


            val layoutManager = FlexboxLayoutManager(this)
            layoutManager.flexWrap = FlexWrap.WRAP
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.FLEX_START
            layoutManager.alignItems = AlignItems.FLEX_START

            val layoutManager2 = FlexboxLayoutManager(this)
            layoutManager2.flexWrap = FlexWrap.WRAP
            layoutManager2.flexDirection = FlexDirection.ROW
            layoutManager2.justifyContent = JustifyContent.FLEX_START
            layoutManager2.alignItems = AlignItems.FLEX_START

            try {
                val languagesList = jobInfo.UF_LANGUAGE_ID_ALL?.split(",")?.map {
                    languages.first { lang -> lang.id == it.toInt() }
                }
                lang_rv.layoutManager = layoutManager2
                lang_rv.adapter =
                    LanguagesAdapter(this, languagesList ?: listOf(), true)
            } catch (e: Exception) {
            }


            filters_tv.layoutManager = layoutManager
            if (jobInfo.UF_SKILLS_ID_ALL == "") {
                filters_tv.visibility = View.GONE
            } else {
                filters_tv.visibility = View.VISIBLE
                //filters_tv.adapter = SkillsAdapter(this, (jobInfo.ALL_SKILLS_NAME?:"").split(","), true)
            }

            phone_tv.data = jobInfo.UF_CONTACT_PHONE
            email_tv.data = jobInfo.UF_CONTACT_EMAIL

            lastUpdate_tv.text = "#${jobInfo.UF_JOBS_ID}\n${jobInfo.UF_MODIFED}"
            company_tv.data = jobInfo.COMPANY ?: ""
            //jobType_tv.text = jobInfo.UF_TYPE_OF_JOB_NAME ?: ""

            details_tv.data = jobInfo.DETAIL_TEXT ?: ""
        } catch (e: Exception) {}
    }

    fun closeClick(v: View) = finish()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICTURE -> logo_iv.setImageURI(data?.data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun deleteClick(v: View) = toast("delete stub")

    fun saveClick(v: View) = toast("save stub")

}