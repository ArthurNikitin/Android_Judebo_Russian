package com.byte4b.judebo.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.SkillsAdapter
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.setLeftDrawable
import com.byte4b.judebo.setRightDrawable
import com.byte4b.judebo.utils.Setting
import com.google.android.flexbox.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.preview.view.*


class DetailsActivity : AppCompatActivity() {

    private var job: MyMarker? = null
    private val setting by lazy { Setting(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        supportActionBar?.hide()

        val jobInfo = Gson().fromJson(intent.getStringExtra("marker"), MyMarker::class.java)
        job = jobInfo

        val setting = Setting(this)
        val currency = currencies.firstOrNull { it.id == jobInfo.UF_GROSS_CURRENCY_ID }
        val lang = languages.firstOrNull { currency?.name == it.currency }

        phone_tv.setLeftDrawable(R.drawable.phone)
        email_tv.setLeftDrawable(R.drawable.mail)

        name_tv.text = jobInfo.NAME

        if (jobInfo.UF_DETAIL_IMAGE.isNotEmpty()) {
            Picasso.get()
                .load(jobInfo.UF_DETAIL_IMAGE)
                .placeholder(R.drawable.big_logo_setting)
                .error(R.drawable.big_logo_setting)
                .into(logo_iv)
        }

        if (jobInfo.UF_GROSS_PER_MONTH.isEmpty() || jobInfo.UF_GROSS_PER_MONTH == "0") {
            salary_tv.visibility = View.GONE
        } else {
            salary_tv.visibility = View.VISIBLE
        }
        if (lang?.locale == setting.language) {
            salary_tv.text = jobInfo.UF_GROSS_PER_MONTH + " ${currency?.name ?: ""}"
            salary_tv.setRightDrawable(currency?.icon ?: R.drawable.iusd)
            secondSalary_tv.visibility = View.INVISIBLE
        } else {
            salary_tv.text = jobInfo.UF_GROSS_PER_MONTH + " ${currency?.name ?: ""}"
            salary_tv.setRightDrawable(currency?.icon ?: R.drawable.iusd)
            secondSalary_tv.visibility = View.VISIBLE
            val currency2 = currencies.firstOrNull { it.name == setting.currency }
            secondSalary_tv.text =
                "(≈${jobInfo.UF_GROSS_PER_MONTH.toDouble() * (currency2?.rate ?: 1)} ${currency2?.name ?: "USD"})"
            secondSalary_tv.setRightDrawable(currency2?.icon ?: R.drawable.iusd)
        }

        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.FLEX_START

        filters_tv.layoutManager = layoutManager
        if (jobInfo.UF_SKILLS_ID_ALL == "") {
            filters_tv.visibility = View.GONE
        } else {
            filters_tv.visibility = View.VISIBLE
            filters_tv.adapter = SkillsAdapter(this, jobInfo.ALL_SKILLS_NAME.split(","))
        }

        if (jobInfo.UF_CONTACT_PHONE.isEmpty())
            phone_tv.visibility = View.GONE
        if (jobInfo.UF_CONTACT_EMAIL.isEmpty())
            email_tv.visibility = View.GONE
        phone_tv.text = jobInfo.UF_CONTACT_PHONE
        email_tv.text = jobInfo.UF_CONTACT_EMAIL

        lastUpdate_tv.text = "(#${jobInfo.UF_JOBS_ID}) ${jobInfo.UF_MODIFED}"
        company_tv.text = jobInfo.COMPANY
        jobType_tv.text = jobInfo.UF_TYPE_OF_JOB_ID.toString()

        details_tv.text = jobInfo.DETAIL_TEXT

    }

    fun closeClick(view: View) = finish()
    fun emailSend(view: View) {

        job?.apply {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
            emailIntent.data = Uri.parse("mailto: ${job!!.UF_CONTACT_EMAIL}")
            startActivity(emailIntent)
        }

    }
    fun callClick(view: View) {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE)
                === PackageManager.PERMISSION_GRANTED
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
        val locale = if (setting.language.isNullOrEmpty()) "en" else setting.language!!
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT,
                "https://$locale.judebo.com/search_job/detail.php?job_id=${job?.UF_JOBS_ID}"
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
}