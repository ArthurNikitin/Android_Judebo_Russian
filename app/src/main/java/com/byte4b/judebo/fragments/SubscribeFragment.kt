package com.byte4b.judebo.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import kotlinx.android.synthetic.main.fragment_subscribe.*

class SubscribeFragment(
    @DrawableRes private val bitmap: Int,
    @StringRes private val title: Int,
    @StringRes private val description: Int
) : Fragment(R.layout.fragment_subscribe) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get my sub
        //load subs from store
        //check valid token if sub from playstore
        //  if error - send to server + show free sub
        //load subs prices + hint for 6 and 12 months
        //setOnClickListeners for buttons
        //(if > subs - delete old sub and add new)
        //else add new

        backgroundImage.setImageResource(bitmap)
        title_tv.setText(title)
        description_tv.setText(description)
    }

}