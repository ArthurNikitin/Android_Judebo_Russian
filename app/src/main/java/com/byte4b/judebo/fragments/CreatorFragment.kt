package com.byte4b.judebo.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
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
import io.realm.kotlin.createObject
import io.realm.kotlin.delete
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_creator.*
import java.text.SimpleDateFormat
import java.util.*

class CreatorFragment : Fragment(R.layout.fragment_creator), ServiceListener,
    SwipeRefreshLayout.OnRefreshListener {

    private val realm by lazy { Realm.getDefaultInstance() }
    private val setting by lazy { Setting(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresher.setOnRefreshListener(this)
        run {
            try {
                val vocations = realm.where<VocationRealm>().findAll()
                if (vocations.isEmpty())
                    onRefresh()
                else
                    onMyVocationsLoaded(
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
//PERIOD_UPDATE_JOB_LIST_FOR_USER_IN_MINUTES
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

    @SuppressLint("SimpleDateFormat")
    override fun onMyVocationsLoaded(list: List<Vocation>?) {
        val realmList: List<VocationRealm> = listOf() //read from db
        list?.forEach {

            // Element have UF_JOBS_ID
            val obj = realmList.firstOrNull { voc -> it.UF_JOBS_ID == voc.UF_JOBS_ID }
            if (obj != null)
                //Element founded in Realm
            {


                    val objDate = SimpleDateFormat("dd.mm.yyyy hh:mm:ss")
                        .parse(obj.UF_MODIFED!!)
                    val itDate =
                        if (it.UF_MODIFED == null) Date(0L)
                        else SimpleDateFormat("dd.mm.yyyy hh:mm:ss").parse(it.UF_MODIFED!!)


                    if (itDate!! > objDate)
                        // Update on WEB
                    {
                        //todo: rewrite all params from WEB and UF_APP_JOB_ID
                        if (it.UF_APP_JOB_ID == null) {

                            //todo: ID in REALM
                            // Generate APP_ID = DEVICE ID (exclude special symbols) + a(int)=1, before A 00000, a = XX XXX XXX
                            // found  in REALM  APP_ID, if found, A++

                            //
                        }
                    }else
                        // Web data not actual nothing do
                    {

                    }

            } else
                //Element NOT founded in Realm with ID
            {
                if (it.UF_APP_JOB_ID == null)
                    //JOB CREATED ON WEB
                {
                    //todo: ID in REALM
                    // Generate APP_ID = DEVICE ID (exclude special symbols) + a(int)=1, before A 00000, a = XX XXX XXX
                    // found  in REALM  APP_ID, if found, A++

                    // todo: add to REALM all params from WEB and UF_APP_JOB_ID
                }else
                {
                    //try found  in REALM  by APP_ID ()
                    if (true)
                        //FOUND
                    {
                        val objDate = SimpleDateFormat("dd.mm.yyyy hh:mm:ss")
                            .parse(obj.UF_MODIFED!!)
                        val itDate =
                            if (it.UF_MODIFED == null) Date(0L)
                            else SimpleDateFormat("dd.mm.yyyy hh:mm:ss").parse(it.UF_MODIFED!!)


                        if (itDate!! > objDate)
                        // todo: rewrite to REALM all params from WEB and UF_APP_JOB_ID
                        {
                            //NEW DATA FROM WEB
                            //todo rewrite all

                        }else
                            //OLD DATA from WEB
                        {
                            //todo rewrite only JOB_ID, MODIFED
                        }

                    }else
                        //NOT FOUND
                    {
                        // Generate APP_ID = DEVICE ID (exclude special symbols) + a(int)=1, before A 00000, a = XX XXX XXX
                        // found  in REALM  APP_ID, if found, A++

                        // todo: add to REALM all params from WEB and UF_APP_JOB_ID
                    }
                }
            }
        }

        //удалить из локаль
        realmList.forEach {
            //проити по всем локальным элементам у которых JOB_ID не пустое, если такой элемент не пришел с сервера
            //то удалить запись из REALM
            // if (list.any { serverItem -> serverItem.UF_JOBS_ID == it.UF_JOBS_ID })
        }


        if (list != null) {
            try {
                realm.executeTransaction {
                    it.createObject<VocationRealm>()
                }
            } catch (e: Exception) {}
            try {
                realm.executeTransaction {
                    it.delete<VocationRealm>()
                    try {
                        it.createObject<VocationRealm>()
                    } catch (e: Exception) {}
                    it.copyToRealm(list.map { it.toRealmVersion() })
                }
            } catch (e: Exception) {}
        }
        try {
            refresher.isRefreshing = false
        } catch (e: Exception) {}

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