package com.byte4b.judebo.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.fragments.*
import com.byte4b.judebo.isRtl
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.utils.signers.GoogleAuth
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val setting by lazy { Setting(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        navBar_bnv.layoutDirection =
            if (isRtl(this)) View.LAYOUT_DIRECTION_RTL
            else View.LAYOUT_DIRECTION_LTR

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
                    true
                }
                R.id.bottom_item_setting -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame, SettingFragment())
                        .commit()
                    true
                }
                R.id.bottom_item_creator -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame,
                            if (setting.isAuth) CreatorFragment() else LoginFragment()
                        )
                        .commit()
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

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.any { it is CreatorFragment }
            && (supportFragmentManager.fragments.last { it is CreatorFragment } as CreatorFragment).isFilterModeOn())
            (supportFragmentManager.fragments.last { it is CreatorFragment } as CreatorFragment).filterOff()
        else if (supportFragmentManager.fragments.any { it is SignUpFragment }) {
            restartFragment(LoginFragment())
        } else
            super.onBackPressed()
    }

    fun getLoginFragment(): LoginFragment? {
        supportFragmentManager.fragments.forEach {
            if (it is LoginFragment)
                return it
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (getLoginFragment()?.facebookAuth != null && getLoginFragment()?.facebookAuth!!.isFB)
                getLoginFragment()?.facebookAuth?.callbackManager?.onActivityResult(requestCode, resultCode, data);
            else if (requestCode == GoogleAuth.SIGN_IN_RC) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                getLoginFragment()?.googleAuth?.handleSignInResult(task)
            }
        } catch (e: Exception) {}
    }

}