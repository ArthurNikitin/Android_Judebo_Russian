package com.byte4b.judebo.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import kotlinx.android.synthetic.main.fragment_creator.*

class CreatorFragment : Fragment(R.layout.fragment_creator) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        link_tv.setOnClickListener {
            try {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data = Uri.parse(getString(R.string.add_job_link))
                startActivity(openURL)
            } catch (e: Exception) {}
        }
    }

}