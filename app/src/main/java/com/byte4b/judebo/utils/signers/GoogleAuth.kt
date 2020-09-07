package com.byte4b.judebo.utils.signers

import android.app.Activity
import android.util.Log
import com.byte4b.judebo.R
import com.byte4b.judebo.fragments.LoginFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class GoogleAuth(ctx: Activity, parent: LoginFragment) : ParentAuth(ctx, parent) {

    init {
        isGoogleAuth = true
    }

    companion object{
        const val SIGN_IN_RC = 1
    }

    private val gso
        get() = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(ctx.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestId()
            .build()

    private var mGoogleSignInClient: GoogleSignInClient

    init {
        Log.e("test", "google init")
        mGoogleSignInClient = GoogleSignIn.getClient(ctx, gso)
        mGoogleSignInClient.signOut()
    }

    fun start() = ctx.startActivityForResult(mGoogleSignInClient.signInIntent, SIGN_IN_RC)

    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        Log.e("test", "google handleSignInResult")
        try {
            val account = completedTask.getResult(ApiException::class.java)
            email = account?.email
            serviceId = account?.id
            auth()
            mGoogleSignInClient.signOut()
        } catch (e: ApiException) {
            Log.e("test", "api error: ${e.localizedMessage}")
        }
    }

}