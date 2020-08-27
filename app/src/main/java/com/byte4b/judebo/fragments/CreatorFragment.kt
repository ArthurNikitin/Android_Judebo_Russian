package com.byte4b.judebo.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.VocationEditActivity
import com.byte4b.judebo.adapters.VocationsAdapter
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.models.VocationRealm
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import com.google.gson.Gson
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_creator.*
import java.text.SimpleDateFormat
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

        createNew.setOnClickListener {
            requireContext().startActivity<VocationEditActivity> {
                putExtra("data", Gson().toJson(Vocation()))
            }
        }

        val handler = Handler {
            onRefresh()
            true
        }
        Thread {
            while (true) {
                if (isResumed)
                    handler.sendEmptyMessage(0)
                Thread.sleep(Setting.PERIOD_UPDATE_JOB_LIST_FOR_USER_IN_SECONDS * 1000L)
            }
        }.start()
    }

    private fun getNewId(): String {
        var random = Random.nextLong(0, 99999999).toString()
        random = "0".repeat(8 - random.length) + random
        Log.e("test", random)
        return "${Calendar.getInstance().timeInMillis / 1000}$random"
    }

    @SuppressLint("SimpleDateFormat")
    override fun onMyVocationsLoaded(list: List<Vocation>?) {
        try {
            refresher.isRefreshing = false
        } catch (e: Exception) {}

        val dateFormat = SimpleDateFormat("dd.mm.yyyy hh:mm:ss")
        val realmList = vocationsFromRealm() //read from db

        list?.forEach { objFromServer ->

            // Element have UF_JOBS_ID
            var objFromRealm = realmList.firstOrNull { objFromServer.UF_JOBS_ID == it.UF_JOBS_ID }
            if (objFromRealm != null)
                //Element founded in Realm by ID
            {
                val objDate = dateFormat.parse(objFromRealm.UF_MODIFED!!)
                val itDate =
                    if (objFromServer.UF_MODIFED == null) Date(0L)
                    else dateFormat.parse(objFromServer.UF_MODIFED!!)


                    if (itDate!! > objDate)
                        // Data updated on WEB
                    {
                        objFromRealm.apply {
                            AUTO_TRANSLATE = objFromServer.AUTO_TRANSLATE
                            COMPANY = objFromServer.COMPANY
                            DETAIL_TEXT = objFromServer.DETAIL_TEXT
                            NAME = objFromServer.NAME
                            UF_APP_JOB_ID = objFromServer.UF_APP_JOB_ID
                            UF_CONTACT_EMAIL = objFromServer.UF_CONTACT_EMAIL
                            UF_CONTACT_PHONE = objFromServer.UF_CONTACT_PHONE
                            UF_DETAIL_IMAGE = objFromServer.UF_DETAIL_IMAGE
                            UF_DISABLE = objFromServer.UF_DISABLE
                            UF_GOLD_GROSS_MONTH = objFromServer.UF_GOLD_GROSS_MONTH
                            UF_GOLD_PER_MONTH = objFromServer.UF_GOLD_PER_MONTH
                            UF_GROSS_CURRENCY_ID = objFromServer.UF_GROSS_CURRENCY_ID
                            UF_GROSS_PER_MONTH = objFromServer.UF_GROSS_PER_MONTH
                            UF_JOBS_ID = objFromServer.UF_JOBS_ID
                            UF_LANGUAGE_ID_ALL = objFromServer.UF_LANGUAGE_ID_ALL
                            UF_LOGO_IMAGE = objFromServer.UF_LOGO_IMAGE
                            UF_MAP_POINT = objFromServer.UF_MAP_POINT
                            UF_MAP_RENDERED = objFromServer.UF_MAP_RENDERED
                            UF_MODIFED = objFromServer.UF_MODIFED
                            UF_PREVIEW_IMAGE = objFromServer.UF_PREVIEW_IMAGE
                            UF_SKILLS_ID_ALL = objFromServer.UF_SKILLS_ID_ALL
                            UF_TYPE_OF_JOB_ID = objFromServer.UF_TYPE_OF_JOB_ID
                            UF_USER_ID = objFromServer.UF_USER_ID
                        }
                        //todo: rewrite all params from WEB and UF_APP_JOB_ID
                        if (objFromServer.UF_APP_JOB_ID == null) {
                            objFromRealm.UF_APP_JOB_ID = getNewId()

                            //todo: ID in REALM
                            // Generate APP_ID = DEVICE ID (exclude special symbols) + a(int)=1, before A 00000, a = XX XXX XXX
                            // found  in REALM  APP_ID, if found, A++
                        }
                    }else
                        // Web data not actual nothing do
                    {

                    }

            } else
                //Element NOT founded in Realm by ID
            {
                if (objFromServer.UF_APP_JOB_ID == null)
                    //JOB CREATED ON WEB
                {
                    val tmpObj = objFromServer.toRealmVersion()
                    tmpObj.UF_APP_JOB_ID = getNewId()
                    realm.copyToRealm(tmpObj)

                    //todo: ID in REALM
                    // Generate APP_ID = BIGINT
                    // Timestamp (10 digit)  + 000 000 01 (Random 8 digit)
                    // = DEVICE ID (exclude special symbols) + a(int)=1, before A 00000, a = XX XXX XXX
                    // found  in REALM  APP_ID, if found, A++
                    // todo: add to REALM all params from WEB and UF_APP_JOB_ID
                } else
                    //JOB CREATED IN APP (may be another device)

                {
                    objFromRealm =
                        realmList.firstOrNull { it.UF_APP_JOB_ID == objFromServer.UF_APP_JOB_ID }
                            //try found  in REALM  by APP_ID ()
                            if (objFromRealm != null)
                                    //FOUND in REALM by APP_ID ()
                            {
                                val objDate = dateFormat.parse(objFromRealm.UF_MODIFED!!)
                                val itDate =
                                    if (objFromServer.UF_MODIFED == null) Date(0L)
                                    else dateFormat.parse(objFromServer.UF_MODIFED!!)


                                if (itDate!! > objDate)
                                // todo: rewrite to REALM all params from WEB and UF_APP_JOB_ID
                                {
                                    //NEW DATA FROM WEB
                                    //todo rewrite all ???? APP_ID
                                    objFromRealm.apply {
                                        AUTO_TRANSLATE = objFromServer.AUTO_TRANSLATE
                                        COMPANY = objFromServer.COMPANY
                                        DETAIL_TEXT = objFromServer.DETAIL_TEXT
                                        NAME = objFromServer.NAME
                                        UF_APP_JOB_ID = objFromServer.UF_APP_JOB_ID
                                        UF_CONTACT_EMAIL = objFromServer.UF_CONTACT_EMAIL
                                        UF_CONTACT_PHONE = objFromServer.UF_CONTACT_PHONE
                                        UF_DETAIL_IMAGE = objFromServer.UF_DETAIL_IMAGE
                                        UF_DISABLE = objFromServer.UF_DISABLE
                                        UF_GOLD_GROSS_MONTH = objFromServer.UF_GOLD_GROSS_MONTH
                                        UF_GOLD_PER_MONTH = objFromServer.UF_GOLD_PER_MONTH
                                        UF_GROSS_CURRENCY_ID = objFromServer.UF_GROSS_CURRENCY_ID
                                        UF_GROSS_PER_MONTH = objFromServer.UF_GROSS_PER_MONTH
                                        UF_JOBS_ID = objFromServer.UF_JOBS_ID
                                        UF_LANGUAGE_ID_ALL = objFromServer.UF_LANGUAGE_ID_ALL
                                        UF_LOGO_IMAGE = objFromServer.UF_LOGO_IMAGE
                                        UF_MAP_POINT = objFromServer.UF_MAP_POINT
                                        UF_MAP_RENDERED = objFromServer.UF_MAP_RENDERED
                                        UF_MODIFED = objFromServer.UF_MODIFED
                                        UF_PREVIEW_IMAGE = objFromServer.UF_PREVIEW_IMAGE
                                        UF_SKILLS_ID_ALL = objFromServer.UF_SKILLS_ID_ALL
                                        UF_TYPE_OF_JOB_ID = objFromServer.UF_TYPE_OF_JOB_ID
                                        UF_USER_ID = objFromServer.UF_USER_ID
                                    }

                                }else
                                    //OLD DATA from WEB, need write only JOBS_ID, DATA MODIFED
                                {
                                    //todo rewrite only JOB_ID, MODIFED
                                    objFromRealm.UF_JOBS_ID = objFromServer.UF_JOBS_ID
                                    objFromRealm.UF_MODIFED = objFromServer.UF_MODIFED
                                }

                            } else
                                //NOT FOUND in REALM by APP_ID ()
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

            val objFromServer = list?.firstOrNull { it.UF_JOBS_ID == objFromRealm.UF_JOBS_ID }
            if (objFromServer == null)
                objFromRealm.deleteFromRealm()
            //проити по всем локальным элементам у которых JOB_ID не пустое, если такой элемент не пришел с сервера
            //то удалить запись из REALM
        }

        run {
            try {
                val vocations = realm.where<VocationRealm>().findAll()
                val data = vocations
                            .map { it.toBasicVersion() }
                            .filter {  !it.isHided }
                setList(data)
            } catch (e: Exception) {
                setList(list)
            }
        }
    }

    private fun setList(list: List<Vocation>?) {
        try {
            refresher.isRefreshing = false
        } catch (e: Exception) {}

        vocations_rv.layoutManager = LinearLayoutManager(requireContext())
        vocations_rv
            .addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        vocations_rv.adapter =
            VocationsAdapter(requireContext(), (list ?: listOf()).filterNot { it.isHided })

        if (list == null)
            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
        else if (list.isEmpty())
            Toast.makeText(requireContext(), "Empty", Toast.LENGTH_SHORT).show()
    }

    override fun onRefresh() {
        refresher.isRefreshing = true
        ApiServiceImpl(this).getMyVocations(
            setting.getCurrentLanguage().locale,
            token = "Z4pjjs5t7rt6uJc2uOLWx5Zb",
            login = "judebo.com@gmail.com"
        )
    }

}