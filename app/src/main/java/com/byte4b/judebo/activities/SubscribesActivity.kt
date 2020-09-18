package com.byte4b.judebo.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.byte4b.judebo.R
import com.byte4b.judebo.adapters.SubscribesViewPagerAdapter
import com.byte4b.judebo.fragments.SubscribeFragment
import kotlinx.android.synthetic.main.activity_subscribes.*

class SubscribesActivity : AppCompatActivity(R.layout.activity_subscribes) {

    fun closeClick(v: View) = finish()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewPagerPendingAdapter = SubscribesViewPagerAdapter(supportFragmentManager, listOf(
                SubscribeFragment(
                    R.drawable.subscription_picture_010,
                    R.string.subsription_limit_10_title,
                    R.string.subsription_limit_10_description
                ),
                SubscribeFragment(
                    R.drawable.subscription_picture_050,
                    R.string.subsription_limit_50_title,
                    R.string.subsription_limit_50_description
                ),
                SubscribeFragment(
                    R.drawable.subscription_picture_200,
                    R.string.subsription_limit_200_title,
                    R.string.subsription_limit_200_description
                )
        ))
        viewpager.adapter = viewPagerPendingAdapter
        indicator.attachToViewPager(viewpager)
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {
                //toast(position.toString())
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun onStart() {
        super.onStart()
        supportActionBar?.hide()
    }

}