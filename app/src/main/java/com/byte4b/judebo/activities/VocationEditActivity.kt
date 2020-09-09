package com.byte4b.judebo.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.byte4b.judebo.*
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.LanguagesAdapter
import com.byte4b.judebo.adapters.SkillsAdapter
import com.byte4b.judebo.fragments.DetailsMapFragment
import com.byte4b.judebo.models.*
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.flexbox.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_vocation_edit.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.random.Random


class VocationEditActivity : AppCompatActivity(), ServiceListener {

    private val realm by lazy { Realm.getDefaultInstance() }
    private var job: Vocation? = null
    private val setting by lazy { Setting(this) }
    private val skillsRealm by lazy {
        realm.where<SkillRealm>().findAll().map { it.toBasicVersion() }
    }
    private var isLogoSelected = false

    companion object {
        private const val REQUEST_CROP = 101
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

        name_field.counterMaxLength = Setting.TEXT_LENGTH_MAX_SYMBOLS_JOB_TITLE
        company_field.counterMaxLength = Setting.TEXT_LENGTH_MAX_SYMBOLS_JOB_COMPANY
        detail_field.counterMaxLength = Setting.TEXT_LENGTH_MAX_SYMBOLS_JOB_DETAIL

        val jobInfo: Vocation = try {
            realm.where<VocationRealm>().equalTo("UF_APP_JOB_ID",
                intent.getLongExtra("appId", -1)
            ).findFirst()!!.toBasicVersion()
        } catch (e: Exception) {
            val tmp = Vocation()
            tmp.UF_LANGUAGE_ID_ALL = setting.getCurrentLanguage().id.toString()
            tmp
        }
        if (jobInfo.UF_APP_JOB_ID == null) {
            val loc = getLocation()
            jobInfo.location = listOf(
                loc?.latitude ?: Setting.DEFAULT_LATITUDE,
                loc?.longitude ?: Setting.DEFAULT_LONGITUDE
            )
        }
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

            try {
                if (!jobInfo.UF_DETAIL_IMAGE.isNullOrEmpty()) {
                    if (jobInfo.UF_DETAIL_IMAGE!!.startsWith("http")) {
                        Glide.with(this)
                            .load(jobInfo.UF_DETAIL_IMAGE)
                            .placeholder(R.drawable.edit_page_default_logo)
                            .into(logo_iv)
                    } else {
                        try {
                            Log.e("test", "start base64 decoding")
                            val btm =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    Base64.getDecoder()
                                        .decode(jobInfo.UF_DETAIL_IMAGE!!)
                                else
                                    android.util.Base64.decode(
                                        jobInfo.UF_DETAIL_IMAGE!!,
                                        android.util.Base64.DEFAULT
                                    )
                            logo_iv.setImageBitmap(
                                BitmapFactory.decodeByteArray(
                                    btm,
                                    0,
                                    btm.size
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("test", "base64 error:")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("test", "base64 error")
            }

            try {
                salary_tv.data = if (jobInfo.UF_GROSS_PER_MONTH == null
                    || jobInfo.UF_GROSS_PER_MONTH == 0) ""
                else
                    jobInfo.UF_GROSS_PER_MONTH?.toString()
                val selectedCurrency = currency ?: setting.getCurrentCurrency()
                currencies.indices.forEach {
                    if (currencies[it].id == selectedCurrency.id) {
                        salaryVal_tv.setSelection(it)
                        return@forEach
                    }
                }

                jobInfo.apply {
                    supportFragmentManager.beginTransaction()
                        .add(
                            R.id.containerFragment, DetailsMapFragment(
                                MyMarker(
                                    "", 0,
                                    COMPANY ?: "", DETAIL_TEXT ?: "", (UF_JOBS_ID ?: 0).toInt(),
                                    NAME ?: "", UF_CONTACT_EMAIL ?: "",
                                    UF_CONTACT_PHONE ?: "",
                                    UF_DETAIL_IMAGE ?: "", "",
                                    "",
                                    (UF_GOLD_PER_MONTH ?: 0).toString(),
                                    UF_GROSS_CURRENCY_ID ?: 0,
                                    (UF_GROSS_PER_MONTH ?: 0).toString(),
                                    (UF_JOBS_ID ?: 0L).toInt(), UF_LANGUAGE_ID_ALL ?: "",
                                    null, UF_LOGO_IMAGE, UF_MAP_POINT ?: "",
                                    location[0], location[1], 0,
                                    UF_MODIFED.toString(), UF_PREVIEW_IMAGE ?: "",
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

            lastUpdate_tv.text =
                if (jobInfo.UF_JOBS_ID == null && jobInfo.UF_APP_JOB_ID == null)
                ""
            else if (jobInfo.UF_JOBS_ID == null && jobInfo.UF_APP_JOB_ID != null)
                jobInfo.UF_APP_JOB_ID.toString()
            else if (jobInfo.UF_JOBS_ID != null && jobInfo.UF_APP_JOB_ID == null)
                jobInfo.UF_JOBS_ID.toString()
            else
                "#${jobInfo.UF_JOBS_ID}\n#${jobInfo.UF_APP_JOB_ID}"

            company_tv.data = jobInfo.COMPANY ?: ""
            val types =
                realm.where<JobTypeRealm>().findAll().filter { it.name.trim() != "" }

            jobType_tv.setSelection(
                types.indices.first { i -> types[i].id == jobInfo.UF_TYPE_OF_JOB_ID }
            )

            details_tv.data = jobInfo.DETAIL_TEXT ?: ""
        } catch (e: Exception) {
        }

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
    }

    fun closeClick(v: View) = finish()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CROP -> {
                if (resultCode == Activity.RESULT_OK) {
                    logo_iv.setImageURI(data?.data)
                    isLogoSelected = true
                }
            }
            REQUEST_PICTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    logo_iv.setImageURI(data?.data)
                    val bitmap = logo_iv.drawable.toBitmap(100, 100)
                    logo_iv.setImageBitmap(bitmap)
                    isLogoSelected = true
                }//tmp stub
                return
                val cropIntent = Intent("com.android.camera.action.CROP")
                cropIntent.setDataAndType(data?.data, "image/*")
                cropIntent.putExtra("crop", "true")
                cropIntent.putExtra("aspectX", 1)
                cropIntent.putExtra("aspectY", 1)
                intent.putExtra("outputX", Setting.MAX_IMG_CROP_HEIGHT)
                intent.putExtra("outputY", Setting.MAX_IMG_CROP_HEIGHT)
                //image type
                intent.putExtra("outputFormat", "JPEG")
                cropIntent.putExtra("return-data", true)

                //val outputFileUri = Uri.fromFile(createCropFile())
                cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, data?.data)
                startActivityForResult(cropIntent, REQUEST_CROP)
            }
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
        if (job == null) return

        Log.e("test", "del")
        AlertDialog.Builder(this)
            .setTitle(R.string.request_request_delete_title)
            .setMessage(R.string.request_request_delete_message)
            .setPositiveButton(R.string.request_request_delete_ok) { dialog, _ ->

                Realm.getDefaultInstance().executeTransaction {
                    val vocationRealm = it.where<VocationRealm>()
                        .equalTo("UF_APP_JOB_ID", job!!.UF_APP_JOB_ID)
                        .findFirst()

                    try {
                        vocationRealm?.isHided = true
                        vocationRealm?.COMPANY = null
                        vocationRealm?.DETAIL_TEXT = null
                        vocationRealm?.UF_CONTACT_EMAIL = null
                        vocationRealm?.UF_CONTACT_PHONE = null
                        vocationRealm?.UF_DETAIL_IMAGE = null
                        vocationRealm?.UF_DISABLE = null
                        vocationRealm?.UF_GOLD_PER_MONTH = null
                        vocationRealm?.UF_GROSS_CURRENCY_ID = null
                        vocationRealm?.UF_GROSS_PER_MONTH = null
                        vocationRealm?.UF_LOGO_IMAGE = null
                        vocationRealm?.UF_MAP_POINT = null
                        vocationRealm?.UF_PREVIEW_IMAGE = null

                        vocationRealm?.UF_LANGUAGE_ID_ALL = null
                        vocationRealm?.UF_SKILLS_ID_ALL = null
                        vocationRealm?.UF_TYPE_OF_JOB_ID = null

                        vocationRealm?.UF_MODIFED = Calendar.getInstance().timestamp
                    } catch (e: Exception) {
                        Log.e("test", e.localizedMessage ?: "ErrorMe")
                    }
                    try {
                        ApiServiceImpl(this).deleteVocation(
                            setting.getCurrentLanguage().locale,
                            token = setting.token ?: "",
                            login = setting.email ?: "",
                            vocation = vocationRealm!!.toBasicVersion()
                        )
                    } catch (e: Exception) {
                        onVocationDeleted(false)
                    }
                    finish()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.request_request_delete_cancel) { d, _ -> d.cancel() }
            .show()
    }

    private fun isValidForm(): Boolean {
        return when {
            name_tv.data?.trim().isNullOrEmpty() -> false

            phone_tv.data?.trim().isNullOrEmpty() && email_tv.data?.trim().isNullOrEmpty() -> false

            details_tv.data?.trim().isNullOrEmpty() -> false

            email_tv.data?.contains("@") == false
                && email_tv.data?.contains(".") == false
                && email_tv.data?.trim()?.isEmpty() == false -> false

            else -> true
        }
    }

    private fun validateForm(): Boolean {
        name_tv.error = null
        phone_tv.error = null
        email_tv.error = null
        details_tv.error = null


        if (name_tv.data?.trim().isNullOrEmpty()) {
            name_tv.error = ""
        }

        if (phone_tv.data?.trim().isNullOrEmpty() && email_tv.data?.trim().isNullOrEmpty()) {
            phone_tv.error = ""
            email_tv.error = ""
        }

        if (details_tv.data?.trim().isNullOrEmpty()) {
            details_tv.error = ""
        }

        if (email_tv.data?.contains("@") == false
            && email_tv.data?.contains(".") == false
            && email_tv.data?.trim()?.isEmpty() == false) {
            email_tv.error = ""
        }

        return true
    }

    @SuppressLint("SimpleDateFormat")
    fun saveClick(v: View) {
        try {
            if (!validateForm()) return

            realm.executeTransaction {
                if (job != null && job!!.UF_APP_JOB_ID != null) {
                    job?.apply {
                        val currentVocationRealm =
                            it.where<VocationRealm>()
                                .equalTo("UF_APP_JOB_ID", job!!.UF_APP_JOB_ID)
                                .findFirst() //if null - create new else edit

                        if (currentVocationRealm == null) {
                            createNewVocation()
                            return@apply
                        }

                        //edit current
                        currentVocationRealm.COMPANY = company_tv.data
                        currentVocationRealm.NAME = name_tv.data
                        currentVocationRealm.DETAIL_TEXT = details_tv.data
                        currentVocationRealm.UF_CONTACT_EMAIL = email_tv.data
                        currentVocationRealm.UF_CONTACT_PHONE = phone_tv.data
                        currentVocationRealm.UF_ACTIVE = if (isValidForm()) 1 else 0

                        val time = Calendar.getInstance()
                        currentVocationRealm.UF_MODIFED = time.timestamp
                        time.add(Calendar.DATE, Setting.JOB_LIFETIME_IN_DAYS)
                        currentVocationRealm.UF_DISABLE = time.timestamp

                        currentVocationRealm.UF_LANGUAGE_ID_ALL = job!!.UF_LANGUAGE_ID_ALL
                        currentVocationRealm.UF_SKILLS_ID_ALL =
                            if (job!!.UF_SKILLS_ID_ALL.isNullOrEmpty()) Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN
                            else job!!.UF_SKILLS_ID_ALL

                        currentVocationRealm.UF_GROSS_PER_MONTH = try {
                                salary_tv.data?.trim()?.replace(" ", "")?.toInt()
                        } catch (e: Exception) {
                            null
                        }
                        currentVocationRealm.UF_GROSS_CURRENCY_ID =
                            currencies[salaryVal_tv.selectedItemPosition].id
                        if (currentVocationRealm.UF_GROSS_PER_MONTH == 0)
                            currentVocationRealm.UF_GROSS_PER_MONTH = null
                        if (currentVocationRealm.UF_GROSS_PER_MONTH != null) {
                            val currency = currencies.first { it.id ==
                                    currentVocationRealm.UF_GROSS_CURRENCY_ID}
                            currentVocationRealm.UF_GOLD_PER_MONTH =
                                ((1000000L * currentVocationRealm.UF_GROSS_PER_MONTH!!)
                                        / currency.rate).toInt()
                        }

                        if (isLogoSelected) {
                            val drawable = logo_iv.drawable
                            currentVocationRealm.UF_DETAIL_IMAGE = toBase64(
                                drawable.toBitmap(
                                    Setting.MAX_IMG_CROP_HEIGHT,
                                    Setting.MAX_IMG_CROP_HEIGHT
                                )
                            )
                            currentVocationRealm.UF_LOGO_IMAGE = toBase64(
                                drawable.toBitmap(
                                    Setting.MAX_IMG_CROP_HEIGHT_LOGO,
                                    Setting.MAX_IMG_CROP_HEIGHT_LOGO
                                )
                            )
                            currentVocationRealm.UF_PREVIEW_IMAGE = toBase64(
                                drawable.toBitmap(
                                    Setting.MAX_IMG_CROP_HEIGHT_PREVIEW,
                                    Setting.MAX_IMG_CROP_HEIGHT_PREVIEW
                                )
                            )
                        }

                        val latLng =
                            (supportFragmentManager.fragments.last() as DetailsMapFragment).latLng
                                ?: LatLng(Setting.DEFAULT_LATITUDE, Setting.DEFAULT_LONGITUDE)
                        currentVocationRealm.UF_MAP_POINT =
                            "${latLng.latitude}, ${latLng.longitude}"
                        currentVocationRealm.UF_TYPE_OF_JOB_ID = realm
                            .where<JobTypeRealm>().findAll()
                            .filter { it.name.trim() != "" }[jobType_tv.selectedItemPosition].id

                        ApiServiceImpl(this).updateMyVocations(
                            setting.getCurrentLanguage().locale,
                            token = setting.token ?: "",
                            login = setting.email ?: "",
                            vocations = listOf(currentVocationRealm.toBasicVersion())
                        )
                        if (isValidForm()) finish()
                    }
                } else
                    createNewVocation()
            }
        } catch (e: Exception) {
            Log.e("test", e.localizedMessage?: "error")
        }
    }

    private fun toBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Base64.getEncoder().encodeToString(stream.toByteArray())
        else
            android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.DEFAULT)
    }

    override fun onMyVocationUpdated(success: Boolean) {
        if (!success)
            toast(R.string.error_no_internet)
    }

    private fun createNewVocation() {
        val currentVocationRealm = VocationRealm()
        //set params
        currentVocationRealm.COMPANY = company_tv.data
        currentVocationRealm.NAME = name_tv.data
        currentVocationRealm.DETAIL_TEXT = details_tv.data
        currentVocationRealm.UF_CONTACT_EMAIL = email_tv.data
        currentVocationRealm.UF_CONTACT_PHONE = phone_tv.data
        currentVocationRealm.UF_ACTIVE = if (isValidForm()) 1 else 0

        val time = Calendar.getInstance()
        currentVocationRealm.UF_MODIFED = time.timestamp
        time.add(Calendar.DATE, Setting.JOB_LIFETIME_IN_DAYS)
        currentVocationRealm.UF_DISABLE = time.timestamp

        currentVocationRealm.UF_LANGUAGE_ID_ALL = job!!.UF_LANGUAGE_ID_ALL
        currentVocationRealm.UF_SKILLS_ID_ALL =
            if (job!!.UF_SKILLS_ID_ALL.isNullOrEmpty()) Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN
            else job!!.UF_SKILLS_ID_ALL

        currentVocationRealm.UF_GROSS_PER_MONTH = try {
            salary_tv.data?.trim()?.replace(" ", "")?.toInt()
        } catch (e: Exception) {
            null
        }
        currentVocationRealm.UF_GROSS_CURRENCY_ID =
            currencies[salaryVal_tv.selectedItemPosition].id
        if (currentVocationRealm.UF_GROSS_PER_MONTH == 0)
            currentVocationRealm.UF_GROSS_PER_MONTH = null
        if (currentVocationRealm.UF_GROSS_PER_MONTH != null) {
            val currency = currencies.first { it.id ==
                    currentVocationRealm.UF_GROSS_CURRENCY_ID}
            currentVocationRealm.UF_GOLD_PER_MONTH =
                ((1000000L * currentVocationRealm.UF_GROSS_PER_MONTH!!)
                        / currency.rate).toInt()
        }

        if (isLogoSelected) {
            val drawable = logo_iv.drawable
            currentVocationRealm.UF_DETAIL_IMAGE = toBase64(
                drawable.toBitmap(Setting.MAX_IMG_CROP_HEIGHT, Setting.MAX_IMG_CROP_HEIGHT)
            )
            currentVocationRealm.UF_LOGO_IMAGE = toBase64(
                drawable.toBitmap(
                    Setting.MAX_IMG_CROP_HEIGHT_LOGO,
                    Setting.MAX_IMG_CROP_HEIGHT_LOGO
                )
            )
            currentVocationRealm.UF_PREVIEW_IMAGE = toBase64(
                drawable.toBitmap(
                    Setting.MAX_IMG_CROP_HEIGHT_PREVIEW,
                    Setting.MAX_IMG_CROP_HEIGHT_PREVIEW
                )
            )
        }

        val latLng =
            (supportFragmentManager.fragments.last() as DetailsMapFragment).latLng ?: LatLng(
                Setting.DEFAULT_LATITUDE,
                Setting.DEFAULT_LONGITUDE
            )
        currentVocationRealm.UF_MAP_POINT = "${latLng.latitude}, ${latLng.longitude}"
        currentVocationRealm.UF_TYPE_OF_JOB_ID = realm
            .where<JobTypeRealm>().findAll()
            .filter { it.name.trim() != "" }[jobType_tv.selectedItemPosition].id

        currentVocationRealm.UF_APP_JOB_ID = getNewJobAppId().toLongOrNull()

        realm.copyToRealm(currentVocationRealm)

        ApiServiceImpl(this).updateMyVocations(
            setting.getCurrentLanguage().locale,
            token = setting.token ?: "",
            login = setting.email ?: "",
            vocations = listOf(currentVocationRealm.toBasicVersion())
        )
        if (isValidForm()) finish()
        else job = currentVocationRealm.toBasicVersion()
    }

    private fun getNewJobAppId(): String {
        var random = Random.nextLong(0, 99999999).toString()
        random = "0".repeat(8 - random.length) + random
        return "${Calendar.getInstance().timeInMillis / 1000}$random"
    }

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
                AlertDialog.Builder(this)
                    .setTitle(R.string.request_image_access_title)
                    .setMessage(R.string.request_image_access_message)
                    .setPositiveButton(R.string.request_geolocation_ok) { dialog, _ ->
                        e.askAgain()
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.request_geolocation_cancel) { d, _ -> d.cancel()}
                    .show()
            }

            if (e.hasForeverDenied()) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.request_image_access_title)
                    .setMessage(R.string.request_image_access_message)
                    .setPositiveButton(R.string.request_geolocation_ok) { dialog, _ ->
                        e.goToSettings()
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.request_geolocation_cancel) { d, _ -> d.cancel()}
                    .show()
            }
        }
    }

}