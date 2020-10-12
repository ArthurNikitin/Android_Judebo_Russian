package com.byte4b.judebo.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.LayoutDirection
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.text.layoutDirection
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient
import com.byte4b.judebo.*
import com.byte4b.judebo.activities.MainActivity
import com.byte4b.judebo.activities.SelectAppCurrency
import com.byte4b.judebo.activities.SelectAppLanguage
import com.byte4b.judebo.activities.SubscribesActivity
import com.byte4b.judebo.models.*
import com.byte4b.judebo.services.ApiServiceImpl
import com.byte4b.judebo.utils.RealmDb
import com.byte4b.judebo.utils.Setting
import com.byte4b.judebo.view.ServiceListener
import es.dmoral.toasty.Toasty
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.delete
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setting.*
import java.util.*

class SettingFragment : Fragment(R.layout.fragment_setting), ServiceListener {

    companion object {
        const val SELECT_LANGUAGE_REQUEST = 11
        const val SELECT_CURRENCY_REQUEST = 12
    }

    private val parent by lazy { requireActivity() as MainActivity }
    private val setting by lazy { Setting(requireActivity()) }
    private val realm by lazy {
        try {
            Realm.getDefaultInstance()
        } catch (e: Exception) {
            Realm.init(requireContext())
            Realm.getDefaultInstance()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showCurrentCurrency()

        try {
            val lang = setting.getCurrentLanguage()
                lang_tv.text = lang.title
                langIcon_iv.setImageResource(lang.flag)
        } catch (e: Exception) {}

        blockedMe_tv.text =
            if (setting.subscribeInfo?.SUBSCRIPTION_STORE_ID == Setting.DEFAULT_SUBSCRIPTION_ID_HOLDEN_ACCOUNT)
                requireContext().getString(R.string.user_hold_account_unhold)
            else
                requireContext().getString(R.string.user_hold_account_hold)

        try {
            if (BuildConfig.DEBUG) {
                val mLocationManager =
                    requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val providers = mLocationManager.getProviders(true)
                var bestLocation: Location? = null
                var result = ""
                for (provider in providers) {
                    val l = mLocationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || l.accuracy < bestLocation.accuracy)
                        bestLocation = l
                    result += "${l.latitude}=${l.longitude}=${l.accuracy}\n"
                }

                result += "\nSelected: ${bestLocation?.latitude}=${bestLocation?.longitude}=${bestLocation?.accuracy}"
                textView2?.text = result
            }
        } catch (e: Exception) {}

        try {
            RealmDb.getVocationsCount(realm)
        } catch (e: Exception) {
            Realm.init(requireContext())
        }

        val count = RealmDb.getVocationsCount(realm)
        subscribe_tv.text = "${setting.subscribeInfo?.SUBSCRIPTION_STORE_ID?.toSubscribeName(realm)
            ?: getString(R.string.user_jobs_list_have_not_subsription)}: $count/${setting.maxVocations}"
        if (setting.maxVocations < count) {
            subscribe_tv.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            subscribe_tv.setTypeface(subscribe_tv.typeface, Typeface.BOLD)
        } else if (setting.maxVocations == count) {
            subscribe_tv.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            subscribe_tv.setTypeface(subscribe_tv.typeface, Typeface.BOLD)
        } else {
            subscribe_tv.setTextColor(resources.getColor(android.R.color.black))
            subscribe_tv.setTypeface(subscribe_tv.typeface, Typeface.NORMAL)
        }

        try {
            if (setting.currency == "") {
                if (setting.language == "") {
                    val currentCurrency =
                        currencies.first { it.name == getLangFromLocale().currency }
                    currency_tv.text = currentCurrency.name
                    currencyIcon_iv.setImageResource(currentCurrency.icon)
                } else {
                    val currentCurrency = currencies.first { it.name == setting.currency }
                    currency_tv.text = currentCurrency.name
                    currencyIcon_iv.setImageResource(currentCurrency.icon)
                }
            } else {
                val currentCurrency = currencies.first { it.name == setting.currency }
                currency_tv.text = currentCurrency.name
                currencyIcon_iv.setImageResource(currentCurrency.icon)
            }
        } catch (e: Exception) {
        }

        deleteMe_tv.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_delete_account_request_title)
                .setMessage(R.string.settings_delete_account_request_message)
                .setPositiveButton(R.string.settings_delete_account_request_delete) { dialog, _ ->
                    realm.executeTransaction {
                        val vocations = it.where<VocationRealm>().findAll()
                        vocations.forEach {
                            //set main field null for delete
                            //map, modif, isHidden
                            it.isHided = true
                            it.UF_MAP_POINT = null
                            it.UF_MODIFED = Calendar.getInstance().timestamp
                        }
                        ApiServiceImpl(this).updateMyVocations(
                            setting.getCurrentLanguage().locale,
                            setting.token ?: "",
                            setting.email ?: "",
                            vocations.map { it.toBasicVersion() }
                        )
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.settings_delete_account_request_cancel) { d, _ ->
                    d.cancel()
                }
                .show()
        }

        signOut_tv.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.request_logout_title)
                .setMessage(R.string.request_logout_message)
                .setPositiveButton(R.string.settings_logout_ok) { dialog, _ ->
                    setting.logout()
                    deleteMe_tv.visibility = View.GONE
                    signOut_tv.visibility = View.GONE
                    myEmail_tv.visibility = View.GONE
                    deleteTitle.visibility = View.GONE
                    Realm.getDefaultInstance().executeTransaction {
                        try {
                            it.delete<VocationRealm>()
                        } catch (e: Exception) {}
                        try {
                            it.createObject<VocationRealm>()
                        } catch (e: Exception) {}
                    }
                    dialog.dismiss()
                    (requireActivity() as MainActivity).navBar_bnv.selectedItemId = R.id.bottom_item_creator
                    (requireActivity() as MainActivity).restartFragment(LoginFragment())
                }
                .setNegativeButton(R.string.settings_logout_cancel) { d, _ -> d.cancel()}
                .show()
        }
        langClickable.setOnClickListener { showLanguageDialog() }
        currencyClickable.setOnClickListener { showCurrencyDialog() }
        support_tv.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
            emailIntent.data = Uri.parse("mailto: ${getString(R.string.settings_title_support_email)}")
            startActivity(emailIntent)
        }
        subsClickable.setOnClickListener { requireContext().startActivity<SubscribesActivity>() }

        blockedMe_tv.setOnClickListener { blockMe() }

        if (setting.toLogin) {
            ApiServiceImpl(this).checkMySub(
                setting.getCurrentLanguage().locale,
                setting.token ?: "",
                setting.email ?: ""
            )
            setting.toLogin = false
        }
    }

    private fun blockMe() {
        if (setting.subscribeInfo?.SUBSCRIPTION_STORE_ID != Setting.DEFAULT_SUBSCRIPTION_ID_HOLDEN_ACCOUNT) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.user_hold_account_hold_request_title)
                .setMessage(R.string.user_hold_account_hold_request_text)
                .setPositiveButton(R.string.user_hold_account_hold_request_yes) { dialog, _ ->

                    //todo:

                    val sub = realm.where<SubscriptionRealm>()
                        .equalTo("ID", Setting.DEFAULT_SUBSCRIPTION_ID_HOLDEN_ACCOUNT.toInt())
                        .findFirst() ?: return@setPositiveButton


                    ApiServiceImpl(this).setSubs(
                        setting.getCurrentLanguage().locale,
                        setting.token ?: "",
                        setting.email ?: "",
                        subsId = Setting.DEFAULT_SUBSCRIPTION_ID_HOLDEN_ACCOUNT,
                        subsEnd = (Calendar.getInstance().timestamp + sub.UF_DURATION).toString(),
                        storeToken = "efwefwfwef"
                    )

                    //send new sub
                    //save new sub local

                    (requireContext() as MainActivity).restartFragment(SettingFragment())
                }
                .setNegativeButton(R.string.user_hold_account_hold_request_cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.user_hold_account_unhold_request_title)
                .setMessage(R.string.user_hold_account_unhold_request_text)
                .setPositiveButton(R.string.user_hold_account_unhold_request_yes) { dialog, _ ->

                    //todo:
                    //send new sub
                    //save new sub local

                    (requireContext() as MainActivity).restartFragment(SettingFragment())
                }
                .setNegativeButton(R.string.user_hold_account_unhold_request_cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }

    override fun onSubsInstalled(result: AuthResult?) {
        //todo
    }

    private val billingClient by lazy {
        BillingClient.newBuilder(requireContext())
            .setListener { _, _ -> }
            .enablePendingPurchases()
            .build()
    }

    private fun queryPurchases() =
        billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList

    override fun onMySubLoaded(result: SubAnswer?) {
        //todo
        setting.toLogin = false
        if (result?.STATUS == "success") {
            if (result.SUBSCRIPTION_STORE_ID?.startsWith("playmarket") == true) {
                val mySub = queryPurchases()?.firstOrNull {
                    it.sku == result.SUBSCRIPTION_STORE_ID && it.purchaseToken == result.SUBSCRIPTION_BILL_TOKEN
                }
                if (mySub != null) {
                    setting.subscribeInfo = result
                    Toasty.success(requireActivity(), R.string.subsription_restore_subs_success).show()
                } else {
                    ApiServiceImpl(this).setSubs(
                        setting.getCurrentLanguage().locale,
                        setting.token ?: "",
                        setting.email ?: "",
                        "0", "0", "0"
                    )
                    setting.subscribeInfo = null
                    Toasty.error(requireContext(), R.string.subsription_restore_subs_error).show()
                }
            } else {
                Toasty.success(requireActivity(), R.string.subsription_restore_subs_success).show()
            }
        } else
            Toasty.error(requireContext(), R.string.subsription_restore_subs_error).show()
    }

    override fun onMyVocationUpdated(success: Boolean) {
        if (success) {
            ApiServiceImpl(this).deleteMe(
                setting.getCurrentLanguage().locale,
                setting.email ?: "",
                setting.token ?: ""
            )
        } else
            Toasty.error(requireContext(), R.string.error_no_internet).show()
    }

    override fun onAccountDeleted(result: Result?) {
        if (result?.status == "success") {
            setting.logout()
            Toasty.success(requireContext(), R.string.settings_delete_account_message).show()
            parent.restartFragment(LoginFragment())
        } else if (result != null) {
            Toasty.error(requireContext(), result.data).show()
        } else
            Toasty.error(requireContext(), R.string.error_no_internet)
    }

    fun setCurrency(id: Int) {
        setting.currency = currencies.first { it.id == id }.name
        showCurrentCurrency()
    }

    private fun showCurrencyDialog() {
        requireActivity().startActivityForResult(
            Intent(requireActivity(), SelectAppCurrency::class.java).apply {
                putExtra("id", setting.getCurrentCurrency().id)
            }, SELECT_CURRENCY_REQUEST
        )
    }

    override fun onStart() {
        super.onStart()

        if (setting.isAuth) {
            deleteTitle.visibility = View.VISIBLE
            deleteMe_tv.visibility = View.VISIBLE
            signOut_tv.visibility = View.VISIBLE
            myEmail_tv.visibility = View.VISIBLE
            myEmail_tv.text = setting.email
        } else {
            deleteTitle.visibility = View.GONE
            deleteMe_tv.visibility = View.GONE
            signOut_tv.visibility = View.GONE
            myEmail_tv.visibility = View.GONE
        }
    }

    fun setLanguage(id: Int) {
        val locale = languages.first { it.id == id }.locale
        if (setting.language != locale) {
            setting.lastUpdateDynamicDataFromServer = (0L).toString()
            setting.language = locale
            setLocale(locale)
            setting.isFromRecreate = true
            parent.recreate()
        }
    }

    private fun showLanguageDialog() {
        requireActivity().startActivityForResult(
            Intent(requireActivity(), SelectAppLanguage::class.java), SELECT_LANGUAGE_REQUEST
        )
    }

    private fun showCurrentCurrency() {
        try {
            if (setting.currency != "") {
                val currency = currencies.first { it.name == setting.currency }
                currency_tv.text = currency.name
                currencyIcon_iv.setImageResource(currency.icon)
            } else {
                if (languages.none { it.locale == Locale.getDefault().language }) {
                    val currency = currencies.first { it.name == "USD" }
                    currency_tv.text = currency.name
                    currencyIcon_iv.setImageResource(currency.icon)
                } else {
                    val lang = languages.first { it.locale == Locale.getDefault().language }
                    val currency = currencies.first { it.name == lang.currency }
                    currency_tv.text = currency.name
                    currencyIcon_iv.setImageResource(currency.icon)
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        setting.isLocaleSettingRtl = locale.layoutDirection == LayoutDirection.RTL
        requireActivity().baseContext.resources.apply {
            Locale.setDefault(locale)
            val config = configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            updateConfiguration(config, displayMetrics)
        }
    }

}