package com.byte4b.judebo.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.VocationsAdapter
import com.byte4b.judebo.models.Vocation
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import kotlinx.android.synthetic.main.fragment_creator.*

class CreatorFragment : Fragment(R.layout.fragment_creator), ServiceListener,
    SwipeRefreshLayout.OnRefreshListener {

    private val setting by lazy { Setting(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresher.setOnRefreshListener(this)
        onRefresh()

        link_tv.setOnClickListener {
            try {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data = Uri.parse(getString(R.string.add_job_link))
                startActivity(openURL)
            } catch (e: Exception) {}
        }
    }

    override fun onMyVocationsLoaded(list: List<Vocation>?) {
        refresher.isRefreshing = false
        vocations_rv.layoutManager = LinearLayoutManager(requireContext())
        vocations_rv.adapter = VocationsAdapter(requireContext(), list ?: listOf())

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