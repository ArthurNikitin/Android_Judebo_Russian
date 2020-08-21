package com.byte4b.judebo.activities

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.byte4b.judebo.*
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.LanguagesAdapter
import com.byte4b.judebo.adapters.SkillsAdapter
import com.byte4b.judebo.fragments.DetailsMapFragment
import com.byte4b.judebo.models.MyMarker
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.utils.Setting
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.flexbox.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_vocation_edit.*

class VocationEditActivity : AppCompatActivity() {

    private var job: Vocation? = null
    private val setting by lazy { Setting(this) }

    companion object {
        private const val REQUEST_CAMERA = 101
        private const val REQUEST_PICTURE = 102
    }

    var EditText.data
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
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) {

                val items = arrayOf("Camera", "Gallery")//arrayOf(getString(R.string.camera), getString(R.string.galery))
                AlertDialog.Builder(this).apply {
                    setItems(items) { dialog, item ->
                        when (item) {
                            0 -> startActivityForResult(
                                Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CAMERA
                            )
                            1 -> {
                                val intent = Intent()
                                intent.type = "image/*"
                                intent.action = Intent.ACTION_GET_CONTENT
                                startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                                    REQUEST_PICTURE)
                            }
                        }
                        dialog.dismiss()
                    }
                }

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
                phone_tv.setLeftDrawable(R.drawable.phone, 50)
                email_tv.setLeftDrawable(R.drawable.mail, 50)
            } else {
                phone_tv.setRightDrawable(R.drawable.phone, 50)
                email_tv.setRightDrawable(R.drawable.mail, 50)
            }

            name_tv.setText(jobInfo.NAME)

            if (!jobInfo.UF_DETAIL_IMAGE.isNullOrEmpty()) {
                Picasso.get()
                    .load(jobInfo.UF_DETAIL_IMAGE)
                    .into(logo_iv)
            } else {
                logo_iv.visibility = View.GONE
            }

            try {
                val view = this
                if (jobInfo.UF_GROSS_PER_MONTH.isNullOrEmpty() || jobInfo.UF_GROSS_PER_MONTH == "0") {
                    view.secondContainer.visibility = View.GONE
                    view.salaryContainer.visibility = View.GONE
                } else {
                    view.secondContainer.visibility = View.VISIBLE
                    view.salaryContainer.visibility = View.VISIBLE
                }
                if (currency?.name == setting.getCurrentCurrency().name) {
                    view.salary_tv.text = jobInfo.UF_GROSS_PER_MONTH?.round()
                    view.salaryVal_tv.text = " ${currency.name}"
                    view.salary_tv.setRightDrawable(currency.icon)
                    view.secondContainer.visibility = View.GONE
                } else {
                    view.salary_tv.text = jobInfo.UF_GROSS_PER_MONTH?.round()?.trim()
                    view.salaryVal_tv.text = " ${currency?.name ?: ""}"
                    view.salary_tv.setRightDrawable(currency?.icon ?: R.drawable.iusd)

                    val currency2 = setting.getCurrentCurrency()
                    val convertedSalary =
                        (jobInfo.UF_GROSS_PER_MONTH?.toDouble() ?: .0 * currency2.rate / (currency?.rate ?: 1))
                            .toString().round().trim()
                    view.secondSalary_tv.text = "≈${convertedSalary}"
                    view.secondContainer.visibility =
                        if (convertedSalary == "0") View.GONE
                        else View.VISIBLE
                    view.secondSalaryVal_tv.text = currency2.name
                    view.secondSalary_tv.setRightDrawable(currency2.icon)
                }

                jobInfo.apply {
                    supportFragmentManager.beginTransaction()
                        .add(R.id.containerFragment, DetailsMapFragment(MyMarker(
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
                        )))
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
            } catch (e:Exception) {}


            filters_tv.layoutManager = layoutManager
            if (jobInfo.UF_SKILLS_ID_ALL == "") {
                filters_tv.visibility = View.GONE
            } else {
                filters_tv.visibility = View.VISIBLE
                //filters_tv.adapter = SkillsAdapter(this, (jobInfo.ALL_SKILLS_NAME?:"").split(","), true)
            }

            if (jobInfo.UF_CONTACT_PHONE.isNullOrEmpty())
                phone_tv.visibility = View.GONE
            if (jobInfo.UF_CONTACT_EMAIL.isNullOrEmpty())
                email_tv.visibility = View.GONE

            phone_tv.text = jobInfo.UF_CONTACT_PHONE + " "
            email_tv.data = jobInfo.UF_CONTACT_EMAIL + " "

            lastUpdate_tv.text = "#${jobInfo.UF_JOBS_ID}\n${jobInfo.UF_MODIFED}"
            company_tv.data = jobInfo.COMPANY ?: ""
            //jobType_tv.text = jobInfo.UF_TYPE_OF_JOB_NAME ?: ""

            details_tv.data = jobInfo.DETAIL_TEXT ?: ""
        } catch (e: Exception) {
            Log.e("debug", e.localizedMessage ?: "Details error")
        }
    }

    fun closeClick(v: View) = finish()

    fun fbclick(v: View) {
        val locale = setting.getCurrentLanguage().locale

        val content = ShareLinkContent.Builder()
            .setContentUrl(Uri.parse("https://$locale.judebo.com/search_job/detail.php?job_id=${job?.UF_JOBS_ID}"))
            .build()
        ShareDialog.show(this, content)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CAMERA -> {
                val image = data?.extras?.get("data") as Bitmap
                icon_iv.setImageBitmap(image)
            }
            REQUEST_PICTURE -> icon_iv.setImageURI(data?.data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}