package com.byte4b.judebo.utils.signers

import android.app.Activity
import android.os.Bundle
import com.byte4b.judebo.fragments.LoginFragment
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import org.json.JSONObject

class FacebookAuth(ctx: Activity, parent: LoginFragment) : ParentAuth(ctx, parent) {

    var callbackManager: CallbackManager? = null
    var isFB = false

    init {
        isGoogleAuth = false
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(ctx, listOf("public_profile", "email"))
    }

    fun start() {
        isFB = true
        setUpFacebook()
    }

    private fun setUpFacebook() {
        try {
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        val request =
                            GraphRequest.newMeRequest(loginResult.accessToken)
                            { obj: JSONObject, _: GraphResponse? ->
                                try {
                                    email = if (!obj.has("email")) "" else obj.getString("email")
                                    serviceId = obj.getString("id")

                                    auth()
                                } catch (e: Exception) {
                                }
                            }
                        val parameters = Bundle()
                        parameters.putString("fields", "id, first_name, last_name, email")
                        request.parameters = parameters
                        request.executeAsync()
                    }

                    override fun onCancel() {}

                    override fun onError(exception: FacebookException) {}
                })
        } catch (e: Exception) {
        }
    }

}