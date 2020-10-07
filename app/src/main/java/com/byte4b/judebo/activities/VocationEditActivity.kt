package com.byte4b.judebo.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.Glide
import com.byte4b.judebo.*
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.LanguagesAdapter
import com.byte4b.judebo.adapters.SkillsAdapter
import com.byte4b.judebo.fragments.DetailsMapFragment
import com.byte4b.judebo.models.*
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.RealmDb
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.flexbox.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.theartofdev.edmodo.cropper.CropImage
import es.dmoral.toasty.Toasty
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_vocation_edit.*
import java.util.*
import kotlin.random.Random


class VocationEditActivity : AppCompatActivity(), ServiceListener {

    private val imageView by lazy { ImageView(this) }
    private val realm by lazy { Realm.getDefaultInstance() }
    private var job: Vocation? = null
    private val setting by lazy { Setting(this) }
    private val skillsRealm by lazy {
        realm.where<SkillRealm>().findAll().map { it.toBasicVersion() }
    }
    private var isLogoSelected = false

    companion object {
        const val SELECT_CURRENCY_VOCATION_REQUEST = 101
        const val SERVER_ANIMATION_REQUEST_TIME_IN_MILLISECONDS = 1000L
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

        if (RealmDb.getVocationsCount(realm) > setting.maxVocations) {
            save1.setImageResource(R.drawable.button_save_deny)
            save2.setImageResource(R.drawable.button_save_deny)
        }

        name_field.counterMaxLength = Setting.TEXT_LENGTH_MAX_SYMBOLS_JOB_TITLE
        company_field.counterMaxLength = Setting.TEXT_LENGTH_MAX_SYMBOLS_JOB_COMPANY
        detail_field.counterMaxLength = Setting.TEXT_LENGTH_MAX_SYMBOLS_JOB_DETAIL

        name_tv.doOnTextChanged { _, _, _, _ ->
            if (name_tv.data?.trim() != "")
                name_field.error = null
        }
        details_tv.doOnTextChanged { _, _, _, _ ->
            if (details_tv.data?.trim() != "")
            detail_field.error = null
        }
        email_tv.doOnTextChanged { _, _, _, _ ->
            if (email_tv.data?.contains("@") == true
                    && email_tv.data?.endsWith(".") == false
                    && email_tv.data?.contains(".") == true
                    && email_tv.data?.trim()?.isEmpty() == false)
                email_field.error = null
        }

        val jobInfo: Vocation = try {
            realm.where<VocationRealm>().equalTo(
                "UF_APP_JOB_ID",
                intent.getLongExtra("appId", -1)
            ).findFirst()!!.toBasicVersion()
        } catch (e: Exception) {
            val tmp = Vocation()
            tmp.UF_LANGUAGE_ID_ALL = setting.getCurrentLanguage().id.toString()
            val time = Calendar.getInstance()
            tmp.UF_MODIFED = time.timestamp
            time.add(Calendar.DATE, Setting.JOB_LIFETIME_IN_DAYS)
            tmp.UF_DISABLE = time.timestamp
            tmp.UF_APP_JOB_ID = getNewJobAppId()
            val latLng = LatLng(
                getLocation()?.latitude ?: Setting.DEFAULT_LATITUDE,
                getLocation()?.longitude ?: Setting.DEFAULT_LONGITUDE
            )
            tmp.UF_MAP_POINT = "${latLng.latitude}, ${latLng.longitude}"
            realm.executeTransaction { it.copyToRealm(tmp.toRealmVersion()) }
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

        Glide.with(this)
            .load(R.drawable.edit_page_default_logo)
            .circleCrop()
            .into(logo_iv)

        initJobTypes()
        initCurrencies()

        try {
            val currency = currencies.firstOrNull { it.id == jobInfo.UF_GROSS_CURRENCY_ID }

            showSystemIcons()

            name_tv.setText(jobInfo.NAME)

            try {
                if (!jobInfo.UF_DETAIL_IMAGE.isNullOrEmpty()) {
                    if (jobInfo.UF_DETAIL_IMAGE!!.startsWith("http")) {
                        Glide.with(this)
                            .load(jobInfo.UF_DETAIL_IMAGE)
                            .circleCrop()
                            .placeholder(R.drawable.edit_page_default_logo)
                            .into(logo_iv)
                    } else {
                        try {
                            val btm =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    Base64.getDecoder()
                                        .decode(jobInfo.UF_DETAIL_IMAGE!!)
                                else
                                    android.util.Base64.decode(
                                        jobInfo.UF_DETAIL_IMAGE!!,
                                        android.util.Base64.DEFAULT
                                    )
                            Handler().postDelayed({
                                Glide.with(this)
                                    .load(
                                        BitmapFactory.decodeByteArray(
                                            btm,
                                            0,
                                            btm.size
                                        )
                                    )
                                    .circleCrop()
                                    .into(logo_iv)
                            }, 200)
                        } catch (e: Exception) {
                        }
                    }
                }
            } catch (e: Exception) {
            }

            try {
                salary_tv.data = if (jobInfo.UF_GROSS_PER_MONTH == null
                    || jobInfo.UF_GROSS_PER_MONTH == 0) ""
                else
                    jobInfo.UF_GROSS_PER_MONTH?.toString()
                val selectedCurrency = currency ?: setting.getCurrentCurrency()
                currencies.indices.forEach {
                    if (currencies[it].id == selectedCurrency.id) {
                        salaryVal_tv.text = currencies[it].name
                        salaryIcon_iv.setImageResource(currencies[it].icon)
                        return@forEach
                    }
                }

                salaryIcon_iv.setOnClickListener { showCurrencyDialog() }
                salaryVal_tv.setOnClickListener { showCurrencyDialog() }

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

            with(jobInfo) {
                if (UF_JOBS_ID == null)
                    serverId_tv.visibility = View.GONE
                serverId_tv.text = "#$UF_JOBS_ID"

                if (UF_APP_JOB_ID == null)
                    appId_tv.visibility = View.GONE
                appId_tv.text = UF_APP_JOB_ID.toString().substring(0, 9) + "\n" +
                        UF_APP_JOB_ID.toString().substring(9)
            }

            company_tv.data = jobInfo.COMPANY ?: ""

            try {
                val types =
                    realm.where<JobTypeRealm>().findAll().filter { it.name.trim() != "" }
                spinner.selectedIndex = types.indices
                    .firstOrNull { types[it].id == jobInfo.UF_TYPE_OF_JOB_ID } ?: 0
            } catch (e: Exception) {
            }

            details_tv.data = jobInfo.DETAIL_TEXT ?: ""
        } catch (e: Exception) {
        }

        validateForm()
        mainContainer.setOnClickListener { hideKeyboard() }
        name_tv.hideKeyboard()
    }

    private fun initJobTypes() {
        spinner.setItems(
            realm.where<JobTypeRealm>().findAll().map { it.name }.filter { it.trim() != "" }
        )

        spinner.setOnClickListener {
            jobType_container.setBackgroundResource(R.drawable.green_container)
        }
        spinner.setOnItemSelectedListener { _, _, _, _ ->
            jobType_container.setBackgroundResource(R.drawable.salary_container_background)
        }
        spinner.setOnNothingSelectedListener {
            jobType_container.setBackgroundResource(R.drawable.salary_container_background)
        }
    }

    var currencyAdapter: ArrayAdapter<String>? = null
    private fun initCurrencies() {
        currencyAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            currencies.map { it.name }
        )
    }

    fun closeClick(v: View) = finish()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SELECT_CURRENCY_VOCATION_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    val currency = currencies.first {
                        it.id == data?.getIntExtra("currency", 1) ?: 1
                    }
                    salaryVal_tv.text = currency.name
                    salaryIcon_iv.setImageResource(currency.icon)
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    logo_iv.setImageURI(result.uri)
                    imageView.setImageURI(result.uri)
                    val bitmap = logo_iv.drawable.toBitmap(100, 100)
                    logo_iv.setImageBitmap(bitmap)
                    Glide.with(this)
                        .load(bitmap)
                        .circleCrop()
                        .into(logo_iv)

                    isLogoSelected = true
                }
            }
            REQUEST_PICTURE -> {
                CropImage
                    .activity(data?.data)
                    .setAspectRatio(1, 1)
                    .start(this)
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
                LanguagesAdapter(
                    this,
                    (languagesList ?: listOf(setting.getCurrentLanguage())),
                    isDetails = true, isEditor = true, vocation = jobInfo
                )
        } catch (e: Exception) {}
    }

    private fun showCurrencyDialog() {
        startActivityForResult(Intent(this, SelectAppCurrency::class.java).apply {
           putExtra("id", job?.UF_GROSS_CURRENCY_ID ?: setting.getCurrentCurrency().id)
        }, SELECT_CURRENCY_VOCATION_REQUEST)
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
            filters_tv.adapter = SkillsAdapter(
                this,
                listOf(),
                isDetails = true, isEditor = true, vocation = jobInfo
            )
        } else {
            val vocationSkillsIds = jobInfo.UF_SKILLS_ID_ALL!!
                .split(",")
                .filterNot { it == Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN }
            filters_tv.adapter = SkillsAdapter(this,
                skillsRealm.filter { it.id.toString() in vocationSkillsIds }.map { it.name },
                isDetails = true, isEditor = true, vocation = jobInfo)
        }
    }

    fun deleteClick(v: View) {
        if (job == null) return

        AlertDialog.Builder(this)
            .setTitle(R.string.request_request_delete_title)
            .setMessage(R.string.request_request_delete_message)
            .setPositiveButton(R.string.request_request_delete_ok) { dialog, _ ->

                Realm.getDefaultInstance().executeTransaction {
                    val vocationRealm = it.where<VocationRealm>()
                        .equalTo("UF_APP_JOB_ID", job!!.UF_APP_JOB_ID?.toLong())
                        .findFirst()
                    //Log.e("test edit", "${vocationRealm?.UF_APP_JOB_ID} ")

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
                    }
                    try {
                        ApiServiceImpl(this).deleteVocation(
                            setting.getCurrentLanguage().locale,
                            token = setting.token ?: "",
                            login = setting.email ?: "",
                            vocation = vocationRealm!!.toBasicVersion()
                        )
                        try {
                            refresher.isRefreshing = true
                            Handler().postDelayed({ refresher.isRefreshing = false },
                                SERVER_ANIMATION_REQUEST_TIME_IN_MILLISECONDS)
                        } catch (e: Exception) {}
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

            email_tv.data?.trim().isNullOrEmpty() -> false

            details_tv.data?.trim().isNullOrEmpty() -> false

            email_tv.data?.contains("@") == false
                && email_tv.data?.contains(".") == false
                && email_tv.data?.trim()?.isEmpty() == false -> false

            else -> true
        }
    }

    private fun validateForm(): Boolean {
        name_field.error = null
        email_field.error = null
        detail_field.error = null

        if (name_tv.data?.trim().isNullOrEmpty()) {
            name_field.error = getString(R.string.edit_item_required_field)
        }

        if (email_tv.data?.trim().isNullOrEmpty()) {
            email_field.error = getString(R.string.edit_item_required_field)
        }

        if (details_tv.data?.trim().isNullOrEmpty()) {
            detail_field.error = getString(R.string.edit_item_required_field)
        }

        if (email_tv.data?.contains("@") == false
            && email_tv.data?.contains(".") == false
            && email_tv.data?.trim()?.isEmpty() == false) {
            email_field.error = ""
        }
        //
        if (name_tv.data?.trim().isNullOrEmpty()) {
            name_tv.requestFocus()
            scroll.post { scroll.smoothScrollTo(0, name_tv.top) }
        } else if (email_tv.data?.trim().isNullOrEmpty()) {
            email_tv.requestFocus()
            scroll.post { scroll.smoothScrollTo(0, email_tv.bottom) }
        } else if (details_tv.data?.trim().isNullOrEmpty()) {
            details_tv.requestFocus()
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        } else if (email_tv.data?.contains("@") == false
            && email_tv.data?.contains(".") == false
            && email_tv.data?.trim()?.isEmpty() == false) {
            email_tv.requestFocus()
            scroll.post { scroll.smoothScrollTo(0, email_tv.bottom) }
        }

        showSystemIcons()
        return true
    }

    private fun showSystemIcons() {
        try {
            val phone = resources.getDrawable(R.drawable.edit_page_phone)
                .toBitmap(Setting.CURRENCY_ICON_SIZE * 2, Setting.CURRENCY_ICON_SIZE * 2)
                .toDrawable(resources)

            val email = resources.getDrawable(R.drawable.edit_page_mail)
                .toBitmap(Setting.CURRENCY_ICON_SIZE * 2, Setting.CURRENCY_ICON_SIZE * 2)
                .toDrawable(resources)

            if (!isRtl(this)) {
                phone_field.startIconDrawable = phone
                email_field.startIconDrawable = email
            } else {
                phone_field.endIconDrawable = phone
                email_field.endIconDrawable = email
            }
        } catch (e: Exception) {
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun saveClick(v: View) {
        if (RealmDb.getVocationsCount(realm) > setting.maxVocations) return
        try {
            if (!validateForm()) return

            realm.executeTransaction {
                if (job != null && job!!.UF_APP_JOB_ID != null) {
                    job?.apply {
                        val currentVocationRealm =
                            it.where<VocationRealm>()
                                .equalTo("UF_APP_JOB_ID", job!!.UF_APP_JOB_ID!!.toLong())
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

                        currentVocationRealm.setDateModifiedAndDisable()

                        currentVocationRealm.UF_LANGUAGE_ID_ALL = job!!.UF_LANGUAGE_ID_ALL
                        currentVocationRealm.UF_SKILLS_ID_ALL =
                            if (job!!.UF_SKILLS_ID_ALL.isNullOrEmpty()) Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN
                            else job!!.UF_SKILLS_ID_ALL

                        currentVocationRealm.setSalary(
                            currencies.firstOrNull { it.name == salaryVal_tv.text }?.id ?: 0,
                            salary_tv.data, realm
                        )

                        if (isLogoSelected)
                            currentVocationRealm.setIcons(imageView.drawable)

                        val latLng =
                            getMapFragment()?.latLng
                                ?: LatLng(Setting.DEFAULT_LATITUDE, Setting.DEFAULT_LONGITUDE)
                        currentVocationRealm.UF_MAP_POINT =
                            "${latLng.latitude}, ${latLng.longitude}"
                        try {
                            currentVocationRealm.UF_TYPE_OF_JOB_ID = realm
                                .where<JobTypeRealm>().findAll()
                                .filter { it.name.trim() != "" }[spinner.selectedIndex]
                                ?.id
                        } catch (e: Exception) {
                            currentVocationRealm.UF_TYPE_OF_JOB_ID = realm
                                .where<JobTypeRealm>().findFirst()?.id
                        }

                        ApiServiceImpl(this).updateMyVocations(
                            setting.getCurrentLanguage().locale,
                            token = setting.token ?: "",
                            login = setting.email ?: "",
                            vocations = listOf(currentVocationRealm.toBasicVersion())
                        )
                        try {
                            refresher.isRefreshing = true
                            Handler().postDelayed({ refresher.isRefreshing = false },
                            SERVER_ANIMATION_REQUEST_TIME_IN_MILLISECONDS)
                        } catch (e: Exception) {}
                        if (isValidForm()) finish()
                    }
                } else
                    createNewVocation()
            }
        } catch (e: Exception) {
        }
    }

    override fun onVocationDeleted(success: Boolean) {
        try { refresher.isRefreshing = false } catch (e: Exception) { e.toLog() }
    }

    override fun onVocationAdded(success: Boolean) {
        try { refresher.isRefreshing = false } catch (e: Exception) { e.toLog() }
    }

    override fun onMyVocationUpdated(success: Boolean) {
        try { refresher.isRefreshing = false } catch (e: Exception) { e.toLog() }
        if (!success)
            Toasty.error(this, R.string.error_no_internet).show()
    }

    private fun Exception.toLog(tag: String = "test") {}

    private fun getMapFragment(): DetailsMapFragment? =
        supportFragmentManager.fragments.lastOrNull { it is DetailsMapFragment } as DetailsMapFragment?

    private fun createNewVocation() {
        val currentVocationRealm = VocationRealm()
        //set params
        currentVocationRealm.COMPANY = company_tv.data
        currentVocationRealm.NAME = name_tv.data
        currentVocationRealm.DETAIL_TEXT = details_tv.data
        currentVocationRealm.UF_CONTACT_EMAIL = email_tv.data
        currentVocationRealm.UF_CONTACT_PHONE = phone_tv.data
        currentVocationRealm.UF_ACTIVE = if (isValidForm()) 1 else 0

        currentVocationRealm.setDateModifiedAndDisable()

        currentVocationRealm.UF_LANGUAGE_ID_ALL = job!!.UF_LANGUAGE_ID_ALL
        currentVocationRealm.UF_SKILLS_ID_ALL =
            if (job!!.UF_SKILLS_ID_ALL.isNullOrEmpty()) Setting.DEFAULT_SKILL_ID_ALWAYS_HIDDEN
            else job!!.UF_SKILLS_ID_ALL

        currentVocationRealm.setSalary(
            currencies.firstOrNull { it.name == salaryVal_tv.text }?.id ?: 0,
            salary_tv.data, realm
        )

        if (isLogoSelected)
            currentVocationRealm.setIcons(imageView.drawable)

        val latLng =
            getMapFragment()?.latLng ?: LatLng(
                Setting.DEFAULT_LATITUDE,
                Setting.DEFAULT_LONGITUDE
            )
        currentVocationRealm.UF_MAP_POINT = "${latLng.latitude}, ${latLng.longitude}"

        currentVocationRealm.UF_TYPE_OF_JOB_ID = realm
            .where<JobTypeRealm>().findAll()
            .filter { it.name.trim() != "" }[spinner.selectedIndex]
            ?.id

        currentVocationRealm.UF_APP_JOB_ID = getNewJobAppId().toLongOrNull()

        realm.copyToRealm(currentVocationRealm)

        ApiServiceImpl(this).updateMyVocations(
            setting.getCurrentLanguage().locale,
            token = setting.token ?: "",
            login = setting.email ?: "",
            vocations = listOf(currentVocationRealm.toBasicVersion())
        )
        try {
            refresher.isRefreshing = true
            Handler().postDelayed({ refresher.isRefreshing = false },
                SERVER_ANIMATION_REQUEST_TIME_IN_MILLISECONDS)
        } catch (e: Exception) {}
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
        hideKeyboard()
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