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

        backgroundImage.setImageResource(bitmap)
        title_tv.setText(title)
        description_tv.setText(description)
    }

}