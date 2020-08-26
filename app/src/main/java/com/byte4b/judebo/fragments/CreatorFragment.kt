package com.byte4b.judebo.fragments

import android.os.Bundle
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
                            .filter { it.UF_JOBS_ID != 0 && it.UF_MODIFED != null }
                    )
            } catch (e: Exception) {}
        }

        createNew.setOnClickListener {
            requireContext().startActivity<VocationEditActivity> {
                putExtra("data", Gson().toJson(Vocation()))
            }
        }
    }

    override fun onMyVocationsLoaded(list: List<Vocation>?) {
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