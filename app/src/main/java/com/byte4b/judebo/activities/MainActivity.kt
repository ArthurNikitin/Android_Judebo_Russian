package com.byte4b.judebo.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.byte4b.judebo.R
import com.byte4b.judebo.fragments.*
import com.byte4b.judebo.isRtl
import com.byte4b.judebo.models.SubAnswer
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.utils.signers.GoogleAuth
import com.byte4b.judebo.view.ServiceListener
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.auth.api.signin.GoogleSignIn
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ServiceListener {

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

            setting.apply {
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
                            when {
                                setting.subscribeInfo?.SUBSCRIPTION_ID == Setting.DEFAULT_SUBSCRIPTION_ID_HOLDEN_ACCOUNT.toInt() ->
                                        BlockedAccountStub()
                                setting.isAuth ->
                                    CreatorFragment()
                                else ->
                                    LoginFragment()
                            }
                        )
                        .commit()
                    true
                }
                else -> true
            }
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {}
            override fun onBillingServiceDisconnected() {}
        })
        checkMySubscription()
    }

    fun checkMySubscription() {
        if (setting.isAuth)
            ApiServiceImpl(this).checkMySub(
                setting.getCurrentLanguage().locale,
                setting.token ?: "",
                setting.email ?: ""
            )
    }

    private val billingClient by lazy {
        BillingClient.newBuilder(this)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()
    }

    private val purchaseUpdateListener by lazy {
        PurchasesUpdatedListener { _, _ -> }
    }

    private fun queryPurchases() =
        billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList

    override fun onMySubLoaded(result: SubAnswer?) {
        //todo
        if (result?.STATUS == "success") {
            if (result.SUBSCRIPTION_STORE_ID?.startsWith("playmarket") == true) {
                val mySub = queryPurchases()?.firstOrNull {
                    it.sku == result.SUBSCRIPTION_STORE_ID
                            //&& it.purchaseToken == result.SUBSCRIPTION_BILL_TOKEN
                }
                if (mySub != null) {
                    setting.subscribeInfo = result
                } else {
                    ApiServiceImpl(this).setSubs(
                        setting.getCurrentLanguage().locale,
                        setting.token ?: "",
                        setting.email ?: "",
                        "0", "0", "0"
                    )
                    setting.subscribeInfo = null
                }
            } else {
                setting.subscribeInfo = result
            }
        } else if (result != null) {
            if (!result.MESSAGE.contains("wrong token", ignoreCase = true))
                Toasty.error(this, result.MESSAGE).show()
        } else
            Toasty.error(this, R.string.error_no_internet).show()
    }

    override fun onStart() {
        super.onStart()
        if (setting.toLogin)
            restartFragment(LoginFragment())
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

    private fun getLoginFragment(): LoginFragment? {
        supportFragmentManager.fragments.reversed().forEach {
            if (it is LoginFragment)
                return it
        }
        return null
    }

    private inline fun <reified F : Fragment> getLastFragment(): F? {
        return supportFragmentManager.fragments.lastOrNull { it is F } as F
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == SettingFragment.SELECT_LANGUAGE_REQUEST) {
                if (resultCode == Activity.RESULT_OK)
                    getLastFragment<SettingFragment>()
                        ?.setLanguage(data?.getIntExtra("langs", -1) ?: -1)
            } else if (requestCode == SettingFragment.SELECT_CURRENCY_REQUEST) {
                if (resultCode == Activity.RESULT_OK)
                    getLastFragment<SettingFragment>()
                        ?.setCurrency(data?.getIntExtra("currency", -1) ?: -1)
            } else if (requestCode == MapsFragment.REQUEST_FILTER) {
                getLastFragment<MapsFragment>()?.onResult(requestCode, resultCode, data)
            } else {
                getLoginFragment()?.apply {
                    if (facebookAuth != null && facebookAuth!!.isFB)
                        facebookAuth?.callbackManager
                            ?.onActivityResult(requestCode, resultCode, data);
                    else if (requestCode == GoogleAuth.SIGN_IN_RC) {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                        googleAuth?.handleSignInResult(task)
                    }
                }
            }
        } catch (e: Exception) {}
    }

}