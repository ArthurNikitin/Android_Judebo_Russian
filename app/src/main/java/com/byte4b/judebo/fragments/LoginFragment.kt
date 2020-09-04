package com.byte4b.judebo.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.MainActivity
import com.byte4b.judebo.models.AuthResult
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.toast
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment(R.layout.fragment_login), ServiceListener {

    private val setting by lazy { Setting(requireContext()) }
    private var email: String? = null

    override fun onDestroy() {
        super.onDestroy()

        try { loginButton_b.dispose() } catch (e: Exception) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginButton_b.setOnClickListener { signInEmail() }
        google_iv.setOnClickListener { signOnGoogle() }
        facebook_iv.setOnClickListener { signInFb() }
    }

    private fun signInEmail() {
        try {
            email_et.error = null
            password_et.error = null

            if (isValid()) {
                ApiServiceImpl(this).signInWithEmail(
                    setting.getCurrentLanguage().locale,
                    email_et.text.toString(),
                    password_et.text.toString()
                )
                email = email_et.text.toString()
                loginButton_b.startAnimation()
                loginButton_b.isEnabled = false
            } else {
                if ((email_et.text?:"").isEmpty())
                    email_et.error = ""
                else if ((password_et.text?:"").isEmpty())
                    password_et.error = ""
            }
        } catch (e: Exception) {
            Log.e("error", "signInEmail: ${e.localizedMessage}")
        }
    }

    private fun signInFb() {
        requireContext().toast("stub")
    }

    private fun signOnGoogle() {
        requireContext().toast("stub")
    }

    private fun isValid(): Boolean {
        return email_et.text.toString().trim().isNotEmpty()
                && password_et.text.toString().isNotEmpty()
    }

    override fun onSignIn(result: AuthResult?) {
        Handler().postDelayed({
            loginButton_b.revertAnimation()
            loginButton_b.isEnabled = true
            if (result?.status == "success") {
                setting.isAuth = true
                setting.email = email
                setting.token = result.data
                (requireActivity() as MainActivity).restartFragment(CreatorFragment())
            } else if (result != null)
                Toasty.error(requireContext(), result.data).show()
            else
                Toasty.error(requireContext(), R.string.error_no_internet).show()
        }, 1000)

    }

}