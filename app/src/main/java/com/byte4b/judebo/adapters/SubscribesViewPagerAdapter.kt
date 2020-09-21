package com.byte4b.judebo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class SubscribesViewPagerAdapter(
    fragmentManager: FragmentManager,
    val fragments: List<Fragment>
) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount() = fragments.size

    override fun getItem(position: Int) = fragments[position]
}