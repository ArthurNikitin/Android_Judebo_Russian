package com.byte4b.judebo.utils.signers

import android.app.Activity
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.MainActivity
import com.byte4b.judebo.fragments.CreatorFragment
import com.byte4b.judebo.fragments.LoginFragment
import com.byte4b.judebo.fragments.SettingFragment
import com.byte4b.judebo.fragments.SignUpFragment
import com.byte4b.judebo.models.AuthResult
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_login.*

open class ParentAuth(val ctx: Activity, val parent: LoginFragment) : ServiceListener {

    private val service by lazy { ApiServiceImpl(this) }
    val setting by lazy { Setting(ctx) }
    var serviceId: String? = null
    var email: String? = null
    var isGoogleAuth: Boolean = false

    private fun stopAnimation() {
        parent.loginButton_b.apply {
            revertAnimation()
            isEnabled = false
        }
    }

    fun auth() {
        parent.loginButton_b.apply {
            startAnimation()
            isEnabled = false
        }
        if (isGoogleAuth)
            service.signInWithGoogle(
                setting.getCurrentLanguage().locale,
                email ?: ""
            )
        else
            service.signInWithFb(
                setting.getCurrentLanguage().locale,
                email ?: ""
            )
    }

    override fun onSignIn(result: AuthResult?) {
        if (result?.status == "success") {
            setting.isAuth = true
            setting.email = email
            setting.token = result.data
            val nav = setting.toLogin
            (ctx as MainActivity).restartFragment(
                if (nav) SettingFragment()
                else CreatorFragment()
            )
        } else if (result != null) {
            setting.toLogin = false
            if (result.data == "user not found") {
                setting.email = email
                setting.signUpFromService = true
                setting.signUpFromGoogle = isGoogleAuth
                (ctx as MainActivity).restartFragment(SignUpFragment())
            } else {
                Toasty.error(ctx, result.data).show()
                stopAnimation()
            }
        } else {
            setting.toLogin = false
            Toasty.error(ctx, R.string.error_no_internet).show()
            stopAnimation()
        }
    }

}