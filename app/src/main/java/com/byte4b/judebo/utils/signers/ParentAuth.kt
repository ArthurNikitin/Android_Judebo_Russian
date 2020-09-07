package com.byte4b.judebo.utils.signers

import android.app.Activity
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.MainActivity
import com.byte4b.judebo.fragments.CreatorFragment
import com.byte4b.judebo.fragments.LoginFragment
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
        parent.loginButton_b.revertAnimation()
        parent.loginButton_b.isEnabled = false
    }

    fun auth() {
        parent.loginButton_b.startAnimation()
        parent.loginButton_b.isEnabled = false
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
            (ctx as MainActivity).restartFragment(CreatorFragment())
        } else if (result != null) {
            //if already exist
            if (result.data == "user not found") {
                if (isGoogleAuth)
                    service.signUpWithGoogle(
                        setting.getCurrentLanguage().locale,
                        email ?: ""
                    )
                else
                    service.signUpWithFb(
                        setting.getCurrentLanguage().locale,
                        email ?: ""
                    )
            }
            else {
                Toasty.error(ctx, result.data).show()
                stopAnimation()
            }
        } else {
            Toasty.error(ctx, R.string.error_no_internet).show()
            stopAnimation()
        }
    }

    override fun onSignUp(result: AuthResult?) {
        if (result?.status == "success") {
            setting.isAuth = true
            setting.email = email
            setting.token = result.data
            (ctx as MainActivity).restartFragment(CreatorFragment())
        } else if (result != null) {
            stopAnimation()
            Toasty.error(ctx, result.data).show()
        } else {
            Toasty.error(ctx, R.string.error_no_internet).show()
            stopAnimation()
        }
    }

}