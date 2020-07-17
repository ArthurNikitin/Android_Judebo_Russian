package com.byte4b.judebo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.byte4b.judebo.R
import com.byte4b.judebo.fragments.MapFragment
import com.byte4b.judebo.fragments.SettingFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navBar_bnv.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_item_map -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame, MapFragment())
                        .commit()
                    supportActionBar?.title = getString(R.string.bottom_map)
                    true
                }
                R.id.bottom_item_setting -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame, SettingFragment())
                        .commit()
                    supportActionBar?.title = getString(R.string.bottom_setting)
                    true
                }
                else -> true
            }
        }
    }
}