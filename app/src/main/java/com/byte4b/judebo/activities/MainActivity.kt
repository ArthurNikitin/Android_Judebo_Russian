package com.byte4b.judebo.activities

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.fragments.MapsFragment
import com.byte4b.judebo.fragments.SettingFragment
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        askPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION) {

            supportFragmentManager
                .beginTransaction()
                .add(R.id.frame, MapsFragment())
                .commit()

        }.onDeclined {
            if (it.hasDenied())
                it.askAgain()
            if (it.hasForeverDenied())
                it.goToSettings()
        }


        navBar_bnv.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_item_map -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame, MapsFragment())
                        .commit()
                    supportActionBar?.title = getString(R.string.search_title_search)
                    true
                }
                R.id.bottom_item_setting -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame, SettingFragment())
                        .commit()
                    supportActionBar?.title = getString(R.string.settings_title_settings)
                    true
                }
                else -> true
            }
        }
    }

    fun restartFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame, fragment)
            .commit()
    }

}