package com.byte4b.judebo.fragments

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.byte4b.judebo.*
import com.byte4b.judebo.activities.MainActivity
import com.byte4b.judebo.activities.SubscribesActivity
import com.byte4b.judebo.activities.VocationEditActivity
import com.byte4b.judebo.adapters.VocationsAdapter
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.models.VocationRealm
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.RealmDb
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.google.gson.Gson
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.delete
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_creator.*
import java.util.*
import kotlin.random.Random

class CreatorFragment : Fragment(R.layout.fragment_creator), ServiceListener,
    SwipeRefreshLayout.OnRefreshListener {

    private val realm by lazy { Realm.getDefaultInstance() }
    private val setting by lazy { Setting(requireContext()) }
    private fun vocationsFromRealm() = realm.where<VocationRealm>().findAll()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresher.setOnRefreshListener(this)

        subscribe_button.setOnClickListener {
            requireContext().startActivity<SubscribesActivity>()
        }

        logout_ll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.request_logout_title)
                .setMessage(R.string.request_logout_message)
                .setPositiveButton(R.string.settings_logout_ok) { dialog, _ ->
                    setting.logout()
                    dialog.dismiss()
                    realm.executeTransaction {
                        try {
                            it.delete<VocationRealm>()
                        } catch (e: Exception) {}
                        try {
                            it.createObject<VocationRealm>()
                        } catch (e: Exception) {}
                    }
                    (requireActivity() as MainActivity).restartFragment(LoginFragment())
                }
                .setNegativeButton(R.string.settings_logout_cancel) { d, _ -> d.cancel()}
                .show()
        }

        createNew.setOnClickListener {
            requireContext().startActivity<VocationEditActivity> {
                putExtra("data", Gson().toJson(Vocation()))
            }
        }

        filter_et.setLeftDrawable(R.drawable.item_detail_tags)
        filter_et.doOnTextChanged { text, _, _, _ ->
            val txt = text.toString()
            val all = realm.where<VocationRealm>().findAll()
                .map { it.toBasicVersion() }
                .filter { !it.isHided }

            if (txt.trim().isEmpty()) {
                setList(all)
                filter_et.setLeftDrawable(R.drawable.item_detail_tags)
            } else {
                setList(all.filter {
                    it.NAME?.contains(txt) == true
                            || it.COMPANY?.contains("txt") == true
                            || it.DETAIL_TEXT?.contains(txt) == true})
                filter_et.setLeftDrawable(R.drawable.button_delete)
            }
        }

        val handler = Handler {
            onRefresh()
            true
        }
        Thread {
            while (true) {
                Thread.sleep(Setting.PERIOD_UPDATE_JOB_LIST_FOR_USER_IN_SECONDS * 1000L)
                if (isResumed)
                    handler.sendEmptyMessage(0)
            }
        }.start()
    }



    fun isFilterModeOn() = filter_et.text.toString().isNotEmpty()

    fun filterOff() = filter_et.setText("")

    @SuppressLint("SetTextI18n")
    private fun showVocationsCount() {
        val vocationsCount = RealmDb.getVocationsCount(realm)
        subscribe_limit.text = "Free: $vocationsCount/${setting.maxVocations}"

        if (vocationsCount == setting.maxVocations) {
            subscribe_limit.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            subscribe_limit.setTypeface(subscribe_limit.typeface, Typeface.BOLD)
        } else {
            subscribe_limit.setTextColor(resources.getColor(android.R.color.white))
            subscribe_limit.setTypeface(subscribe_limit.typeface, Typeface.NORMAL)
        }
    }

    override fun onStart() {
        super.onStart()

        showVocationsCount()

        run {
            try {
                val vocations = vocationsFromRealm()
                if (vocations.isEmpty())
                    onRefresh()
                else
                    setList(
                        vocations
                            .map { it.toBasicVersion() }
                            .filter { !it.isHided }
                    )
            } catch (e: Exception) {}
        }
    }

    private fun getNewJobAppId(): String {
        var random = Random.nextLong(0, 99999999).toString()
        random = "0".repeat(8 - random.length) + random
        return "${Calendar.getInstance().timeInMillis / 1000}$random"
    }

    @SuppressLint("SimpleDateFormat")
    override fun onMyVocationsLoaded(list: List<Vocation>?) {
        try {
            refresher.isRefreshing = false
        } catch (e: Exception) {}
        if (list == null) return

        val realmList = vocationsFromRealm() //read from db

        realm.executeTransaction {
            list.forEach { objFromServer ->

                // Element have UF_JOBS_ID
                var objFromRealm =
                    realmList.firstOrNull { objFromServer.UF_JOBS_ID == it.UF_JOBS_ID }
                if (objFromRealm != null)
                //Element founded in Realm by ID
                {
                    val objDate = getDate(objFromRealm.UF_MODIFED)
                    val itDate = getDate(objFromServer.UF_MODIFED)


                    if (itDate > objDate)
                    // +++ Data updated on WEB
                    {
                        objFromRealm.apply {
                            COMPANY = objFromServer.COMPANY
                            DETAIL_TEXT = objFromServer.DETAIL_TEXT
                            NAME = objFromServer.NAME
                            UF_APP_JOB_ID = objFromServer.UF_APP_JOB_ID
                            UF_CONTACT_EMAIL = objFromServer.UF_CONTACT_EMAIL
                            UF_CONTACT_PHONE = objFromServer.UF_CONTACT_PHONE
                            UF_DETAIL_IMAGE = objFromServer.UF_DETAIL_IMAGE
                            UF_DISABLE = objFromServer.UF_DISABLE
                            UF_GOLD_PER_MONTH = objFromServer.UF_GOLD_PER_MONTH
                            UF_GROSS_CURRENCY_ID = objFromServer.UF_GROSS_CURRENCY_ID
                            UF_GROSS_PER_MONTH = objFromServer.UF_GROSS_PER_MONTH
                            UF_JOBS_ID = objFromServer.UF_JOBS_ID
                            UF_LANGUAGE_ID_ALL = objFromServer.UF_LANGUAGE_ID_ALL
                            UF_LOGO_IMAGE = objFromServer.UF_LOGO_IMAGE
                            UF_MAP_POINT = objFromServer.UF_MAP_POINT
                            UF_MODIFED = objFromServer.UF_MODIFED
                            UF_PREVIEW_IMAGE = objFromServer.UF_PREVIEW_IMAGE
                            UF_SKILLS_ID_ALL = objFromServer.UF_SKILLS_ID_ALL
                            UF_TYPE_OF_JOB_ID = objFromServer.UF_TYPE_OF_JOB_ID
                        }
                        //rewrite all params from WEB and UF_APP_JOB_ID
                        //if (objFromServer.UF_APP_JOB_ID == null) {


                        //IF we have recird in REAL that we have APP_ID
                        // ID in REALM
                        // Generate APP_ID = DEVICE ID (exclude special symbols) + a(int)=1, before A 00000, a = XX XXX XXX
                        // found  in REALM  APP_ID, if found, A++

                        //  objFromRealm.UF_APP_JOB_ID = getNewId()
                        //  }
                    } else
                    // +++ Web data not actual nothing do
                    {

                    }

                } else
                //Element NOT founded in Realm by ID
                {
                    if (objFromServer.UF_APP_JOB_ID == null)
                    // +++ JOB CREATED ON WEB
                    {
                        val tmpObj = objFromServer.toRealmVersion()
                        tmpObj.UF_APP_JOB_ID = getNewJobAppId().toLong()
                        tmpObj.UF_MODIFED = Calendar.getInstance().timestamp
                        val now = Calendar.getInstance()
                        now.add(Calendar.DATE, Setting.JOB_LIFETIME_IN_DAYS)
                        tmpObj.UF_DISABLE = now.timestamp
                        //modified date current
                        realm.copyToRealm(tmpObj)

                        //todo: ID in REALM
                        // Generate APP_ID = BIGINT
                        // Timestamp (10 digit)  + 000 000 01 (Random 8 digit)
                        // = DEVICE ID (exclude special symbols) + a(int)=1, before A 00000, a = XX XXX XXX
                        // found  in REALM  APP_ID, if found, A++
                        // todo: add to REALM all params from WEB and UF_APP_JOB_ID
                    } else
                    // --- JOB CREATED IN APP (may be another device)
                    {
                        objFromRealm =
                            realmList.firstOrNull { it.UF_APP_JOB_ID == objFromServer.UF_APP_JOB_ID }
                        //try found  in REALM  by APP_ID ()
                        if (objFromRealm != null)
                        //FOUND in REALM by APP_ID ()
                        {
                            val objDate = getDate(objFromRealm.UF_MODIFED)
                            val itDate = getDate(objFromServer.UF_MODIFED)

                            //itDate from JSON
                            if (itDate > objDate)
                            // rewrite to REALM all params from WEB and UF_APP_JOB_ID
                            {
                                //NEW DATA FROM WEB
                                //todo rewrite all ???? APP_ID
                                objFromRealm.apply {
                                    COMPANY = objFromServer.COMPANY
                                    DETAIL_TEXT = objFromServer.DETAIL_TEXT
                                    NAME = objFromServer.NAME
                                    UF_APP_JOB_ID = objFromServer.UF_APP_JOB_ID
                                    UF_CONTACT_EMAIL = objFromServer.UF_CONTACT_EMAIL
                                    UF_CONTACT_PHONE = objFromServer.UF_CONTACT_PHONE
                                    UF_DETAIL_IMAGE = objFromServer.UF_DETAIL_IMAGE
                                    UF_DISABLE = objFromServer.UF_DISABLE
                                    UF_GOLD_PER_MONTH = objFromServer.UF_GOLD_PER_MONTH
                                    UF_GROSS_CURRENCY_ID = objFromServer.UF_GROSS_CURRENCY_ID
                                    UF_GROSS_PER_MONTH = objFromServer.UF_GROSS_PER_MONTH
                                    UF_JOBS_ID = objFromServer.UF_JOBS_ID
                                    UF_LANGUAGE_ID_ALL = objFromServer.UF_LANGUAGE_ID_ALL
                                    UF_LOGO_IMAGE = objFromServer.UF_LOGO_IMAGE
                                    UF_MAP_POINT = objFromServer.UF_MAP_POINT
                                    UF_MODIFED = objFromServer.UF_MODIFED
                                    UF_PREVIEW_IMAGE = objFromServer.UF_PREVIEW_IMAGE
                                    UF_SKILLS_ID_ALL = objFromServer.UF_SKILLS_ID_ALL
                                    UF_TYPE_OF_JOB_ID = objFromServer.UF_TYPE_OF_JOB_ID
                                }

                            } else
                            //OLD DATA from WEB, need write only JOBS_ID, (not need: DATA MODIFED, DISABLE)
                            {
                                // rewrite only JOB_ID
                                objFromRealm.UF_JOBS_ID = objFromServer.UF_JOBS_ID
                            }

                        } else
                        //+++ NOT FOUND in REALM by APP_ID ()
                        {
                            // Write element to local DB
                            //
                            val tmpObj = objFromServer.toRealmVersion()
                            realm.copyToRealm(tmpObj)
                            // todo: add to REALM all params from WEB and UF_APP_JOB_ID
                        }
                    }
                }
            }

            //удалить из локаль
            realmList.forEach { objFromRealm ->
                if (objFromRealm.UF_JOBS_ID == null) return@forEach

                val objFromServer = list.firstOrNull { it.UF_JOBS_ID == objFromRealm.UF_JOBS_ID }
                if (objFromServer == null)
                    objFromRealm.deleteFromRealm()
                //проити по всем локальным элементам у которых JOB_ID не пустое, если такой элемент не пришел с сервера
                //то удалить запись из REALM
            }

            // update old data on web server
            val vocationsForUploadToServer = mutableListOf<Vocation>()
            realmList.forEach { vocationFromRealm ->
                if (vocationFromRealm.UF_JOBS_ID == null)
                //if JOBS_ID == null
                {
                    //that element save to array for send to server
                    vocationsForUploadToServer.add(vocationFromRealm.toBasicVersion())
                } else
                //if JOBS_ID != null
                {
                    val vocationFromServer =
                        list.firstOrNull { vocationFromRealm.UF_JOBS_ID == it.UF_JOBS_ID }
                    if (vocationFromServer != null)
                        //if ELEMENT exist in JSON
                    {
                        if (vocationFromRealm.UF_MODIFED ?: 0 > vocationFromServer.UF_MODIFED ?: 0)
                        //if MODIF Realm > MODIF JSON
                        {
                            //that element save to array for send to server
                            vocationsForUploadToServer.add(vocationFromRealm.toBasicVersion())
                        }
                    }
                }
            }

            ApiServiceImpl(this).updateMyVocations(
                setting.getCurrentLanguage().locale,
                token = setting.token ?: "",
                login = setting.email ?: "",
                vocations = vocationsForUploadToServer
            )
        }

        run {
            try {
                val vocations = realm.where<VocationRealm>().findAll()
                val data = vocations
                            .map { it.toBasicVersion() }
                            .filter {  !it.isHided }
                setList(data)
            } catch (e: Exception) { setList(list) }
        }
    }

    private fun setList(list: List<Vocation>?) {
        Log.e("check", "for show")
        list?.forEach {
            Log.e("check", "${it.UF_JOBS_ID}: ${Gson().toJson(it)}")
        }

        Log.e("check", "in realm")
        realm.where<VocationRealm>().findAll().map { it.toBasicVersion() }.forEach {
            Log.e("check", "${it.UF_JOBS_ID}: ${Gson().toJson(it)}")
        }

        try {
            refresher.isRefreshing = false
        } catch (e: Exception) {}

        vocations_rv.layoutManager = LinearLayoutManager(requireContext())
        vocations_rv
            .addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        vocations_rv.adapter =
            VocationsAdapter(requireContext(), (list ?: listOf()).filterNot { it.isHided }, this)

        showVocationsCount()

        if (list == null)
            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
        else if (list.isEmpty())
            Toast.makeText(requireContext(), "Empty", Toast.LENGTH_SHORT).show()
    }

    override fun onRefresh() {
        refresher.isRefreshing = true
        ApiServiceImpl(this).getMyVocations(
            setting.getCurrentLanguage().locale,
            token = setting.token ?: "",
            login = setting.email ?: ""
        )
    }

}