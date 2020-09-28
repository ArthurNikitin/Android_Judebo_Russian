package com.byte4b.judebo.fragments

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.MainActivity
import com.byte4b.judebo.activities.PolicyActivity
import com.byte4b.judebo.hideKeyboard
import com.byte4b.judebo.models.AuthResult
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_sign_up.*

class SignUpFragment : Fragment(R.layout.fragment_sign_up), ServiceListener {

    private val setting by lazy { Setting(requireContext()) }
    private var email: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (setting.signUpFromService) {
            email_et.isEnabled = false
            email_et.setText(setting.email)
        }

        privacy_tv.setOnClickListener {
            requireContext().startActivity<PolicyActivity> {
                putExtra("url", getString(R.string.user_registration_privacy_url))
            }
        }

        agreement_tv.setOnClickListener {
            requireContext().startActivity<PolicyActivity> {
                putExtra("url", getString(R.string.user_registration_agreement_url))
            }
        }

        loginButton_b.setOnClickListener { signUpEmail() }
    }

    private fun isValid(): Boolean {
        return email_et.text.toString().trim().isNotEmpty()
                && password_et.text.toString().isNotEmpty()
                && passwordConfirm_et.text.toString() == password_et.text.toString()
                && privacySwitch.isChecked
                && agreementSwitch.isChecked
    }

    private fun signUpEmail() {
        try {
            email_et.error = null
            password_et.error = null
            passwordConfirm_et.error = null
            privacySwitch.error = null
            agreementSwitch.error = null

            if (isValid()) {
                requireActivity().hideKeyboard()

                if (setting.signUpFromService) {
                    if (setting.signUpFromGoogle)
                        ApiServiceImpl(this).signUpWithGoogle(
                            setting.getCurrentLanguage().locale,
                            email_et.text.toString(),
                            password_et.text.toString()
                        )
                    else
                        ApiServiceImpl(this).signUpWithFb(
                            setting.getCurrentLanguage().locale,
                            email_et.text.toString(),
                            password_et.text.toString()
                        )
                } else {
                    ApiServiceImpl(this).signUpWithEmail(
                        setting.getCurrentLanguage().locale,
                        email_et.text.toString(),
                        password_et.text.toString()
                    )
                }
                email = email_et.text.toString()
                loginButton_b.startAnimation()
                loginButton_b.isEnabled = false
            } else {
                if ((email_et.text?:"").isEmpty())
                    email_et.error = getString(R.string.user_registration_required_field)
                else if ((password_et.text?:"").isEmpty())
                    password_et.error = getString(R.string.user_registration_required_field)
                else if(passwordConfirm_et.text.toString() != password_et.text.toString())
                    passwordConfirm_et.error = ""
                else if (!privacySwitch.isChecked)
                    privacySwitch.error = getString(R.string.user_registration_required_field)
                else if (!agreementSwitch.isChecked)
                    agreementSwitch.error = getString(R.string.user_registration_required_field)
            }
        } catch (e: Exception) {
        }
    }

    override fun onSignUp(result: AuthResult?) {
        setting.signUpFromService = false

        Handler().postDelayed({
            loginButton_b.revertAnimation()
            loginButton_b.isEnabled = true
            if (result?.status == "success") {
                setting.isAuth = true
                setting.email = email
                setting.token = result.token
                (requireActivity() as MainActivity).restartFragment(CreatorFragment())
            } else if (result != null)
                Toasty.error(requireContext(), result.data).show()
            else
                Toasty.error(requireContext(), R.string.error_no_internet).show()
        }, 1000)
    }

}