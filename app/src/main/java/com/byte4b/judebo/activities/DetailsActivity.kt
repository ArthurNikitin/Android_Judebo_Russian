package com.byte4b.judebo.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.byte4b.judebo.*
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.LanguagesAdapter
import com.byte4b.judebo.adapters.SkillsAdapter
import com.byte4b.judebo.fragments.DetailsMapFragment
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.utils.Setting
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.google.android.flexbox.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_details.*
import java.text.SimpleDateFormat


class DetailsActivity : AppCompatActivity() {

    private var job: MyMarker? = null
    private val setting by lazy { Setting(this) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        supportActionBar?.hide()

        try {
            val jobInfo = Gson().fromJson(intent.getStringExtra("marker"), MyMarker::class.java)
            job = jobInfo

            val currency = currencies.firstOrNull { it.id == jobInfo.UF_GROSS_CURRENCY_ID }

            if (!isRtl(this)) {
                phone_tv.setLeftDrawable(R.drawable.item_detail_phone)
                email_tv.setLeftDrawable(R.drawable.item_detail_email)
            } else {
                phone_tv.setRightDrawable(R.drawable.item_detail_phone)
                email_tv.setRightDrawable(R.drawable.item_detail_email)
            }

            name_tv.text = jobInfo.NAME

            if (jobInfo.UF_DETAIL_IMAGE.isNotEmpty()) {
                Picasso.get()
                    .load(jobInfo.UF_DETAIL_IMAGE)
                    .into(logo_iv)
            } else {
                logo_iv.visibility = View.GONE
            }

            try {
                val view = this
                if (jobInfo.UF_GROSS_PER_MONTH.isEmpty() || jobInfo.UF_GROSS_PER_MONTH == "0") {
                    view.secondContainer.visibility = View.GONE
                    view.salaryContainer.visibility = View.GONE
                } else {
                    view.secondContainer.visibility = View.VISIBLE
                    view.salaryContainer.visibility = View.VISIBLE
                }
                if (currency?.name == setting.getCurrentCurrency().name) {
                    view.salary_tv.text = jobInfo.UF_GROSS_PER_MONTH.round()
                    view.salaryVal_tv.text = " ${currency.name}"
                    view.salary_tv.setRightDrawable(currency.icon)
                    view.secondContainer.visibility = View.GONE
                } else {
                    view.salary_tv.text = jobInfo.UF_GROSS_PER_MONTH.round().trim()
                    view.salaryVal_tv.text = " ${currency?.name ?: ""}"
                    view.salary_tv.setRightDrawable(currency?.icon ?: R.drawable.iusd)

                    val currency2 = setting.getCurrentCurrency()
                    val convertedSalary =
                        (jobInfo.UF_GROSS_PER_MONTH.toDouble() * currency2.rate / (currency?.rate ?: 1))
                            .toString().round().trim()
                    view.secondSalary_tv.text = "≈${convertedSalary}"
                    view.secondContainer.visibility =
                        if (convertedSalary == "0") View.GONE
                        else View.VISIBLE
                    view.secondSalaryVal_tv.text = currency2.name
                    view.secondSalary_tv.setRightDrawable(currency2.icon)
                }

                supportFragmentManager.beginTransaction()
                    .add(R.id.containerFragment, DetailsMapFragment(jobInfo))
                    .commit()
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
                val languagesList = jobInfo.UF_LANGUAGE_ID_ALL.split(",").map {
                    languages.first { lang -> lang.id == it.toInt() }
                }
                lang_rv.layoutManager = layoutManager2
                lang_rv.adapter = LanguagesAdapter(this, languagesList, true)
            } catch (e:Exception) {}


            filters_tv.layoutManager = layoutManager
            if (jobInfo.UF_SKILLS_ID_ALL == "") {
                filters_tv.visibility = View.GONE
            } else {
                filters_tv.visibility = View.VISIBLE
                filters_tv.adapter = SkillsAdapter(this,
                    jobInfo.ALL_SKILLS_NAME
                        .split(",")
                        .filterNot { it == Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN },
                    true)
            }

            if (jobInfo.UF_CONTACT_PHONE.isEmpty())
                phone_tv.visibility = View.GONE
            if (jobInfo.UF_CONTACT_EMAIL.isEmpty())
                email_tv.visibility = View.GONE

            phone_tv.text = jobInfo.UF_CONTACT_PHONE + " "
            email_tv.text = jobInfo.UF_CONTACT_EMAIL + " "

            val format = SimpleDateFormat("dd.MM.yyyy hh:mm:ss")
            val modifiedDate = format.parse(jobInfo.UF_MODIFED)
            val showFormat = SimpleDateFormat("dd.MM")

            lastUpdate_tv.text = "#${jobInfo.UF_JOBS_ID}\n${showFormat.format(modifiedDate)}"
            company_tv.text = jobInfo.COMPANY
            jobType_tv.text = jobInfo.UF_TYPE_OF_JOB_NAME ?: ""

            details_tv.text = jobInfo.DETAIL_TEXT
        } catch (e: Exception) {
            Log.e("debug", e.localizedMessage ?: "Details error")
        }
    }

    fun closeClick(v: View) = finish()

    fun emailSend(v: View) {
        job?.apply {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
            emailIntent.data = Uri.parse("mailto: ${job!!.UF_CONTACT_EMAIL}")
            startActivity(emailIntent)
        }
    }

    fun callClick(v: View) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
            ) {
                val i = Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:${job?.UF_CONTACT_PHONE}")
                )
                startActivity(i)
            } else {
                //запрашиваем разрешение
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    1
                )
            }
        }

    }

    fun fbclick(view: View) {
        val locale = setting.getCurrentLanguage().locale

        val content = ShareLinkContent.Builder()
            .setContentUrl(Uri.parse("https://$locale.judebo.com/search_job/detail.php?job_id=${job?.UF_JOBS_ID}"))
            .build()
        ShareDialog.show(this, content)
    }
}