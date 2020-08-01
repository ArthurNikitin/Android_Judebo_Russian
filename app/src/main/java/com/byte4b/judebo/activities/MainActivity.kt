package com.byte4b.judebo.activities

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.fragments.CreatorFragment
import com.byte4b.judebo.fragments.MapsFragment
import com.byte4b.judebo.fragments.SettingFragment
import com.byte4b.judebo.utils.Setting
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        askPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION) {

            Setting(this).apply {
                if (isFromRecreate) {
                    isFromRecreate = false
                    restartFragment(SettingFragment())
                } else {
                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.frame, MapsFragment())
                        .commit()
                }
            }

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
                R.id.bottom_item_creator -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame, CreatorFragment())
                        .commit()
                    supportActionBar?.title = getString(R.string.add_job_title)
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