package com.byte4b.judebo.fragments

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.MainActivity
import com.byte4b.judebo.activities.SubscribesActivity
import com.byte4b.judebo.adapters.CurrencyAdapter
import com.byte4b.judebo.adapters.LanguageAdapter
import com.byte4b.judebo.getLangFromLocale
import com.byte4b.judebo.models.VocationRealm
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.startActivity
import com.byte4b.judebo.utils.Setting
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.delete
import kotlinx.android.synthetic.main.fragment_setting.*
import java.util.*

class SettingFragment : Fragment(R.layout.fragment_setting) {

    private val parent by lazy { requireActivity() as MainActivity }
    private val setting by lazy { Setting(requireActivity()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showCurrentCurrency()

        try {
            val lang = setting.getCurrentLanguage()
                lang_tv.text = lang.title
                langIcon_iv.setImageResource(lang.flag)
        } catch (e: Exception) {
            Log.e("test", "1: " + e.localizedMessage)
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
            Log.e("test", "2: " + e.localizedMessage)
        }

        signOut_tv.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.request_logout_title)
                .setMessage(R.string.request_logout_message)
                .setPositiveButton(R.string.settings_logout_ok) { dialog, _ ->
                    setting.logout()
                    signOut_tv.visibility = View.GONE
                    myEmail_tv.visibility = View.GONE
                    Realm.getDefaultInstance().executeTransaction {
                        try {
                            it.delete<VocationRealm>()
                        } catch (e: Exception) {}
                        try {
                            it.createObject<VocationRealm>()
                        } catch (e: Exception) {}
                    }
                    dialog.dismiss()
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
    }

    private fun showCurrencyDialog() {
        val titles = currencies.map { it.name }

        val currency = setting.getCurrentCurrency()

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_title_currency)
            .setAdapter(CurrencyAdapter(requireContext(), currencies.toTypedArray(), currency.name)) { dialog, index ->
                setting.currency = titles[index]
                showCurrentCurrency()
                dialog.dismiss()
            }
            .show()
    }

    override fun onStart() {
        super.onStart()

        if (setting.isAuth) {
            signOut_tv.visibility = View.VISIBLE
            myEmail_tv.visibility = View.VISIBLE
            myEmail_tv.text = setting.email
        } else {
            signOut_tv.visibility = View.GONE
            myEmail_tv.visibility = View.GONE
        }
    }

    private fun showLanguageDialog() {
        val locales = languages.map { it.locale } //название этих локализаций
        val lang = setting.getCurrentLanguage().locale
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_title_language)
            .setAdapter(LanguageAdapter(requireContext(), languages.toTypedArray(), lang)) { dialogInterface: DialogInterface, i: Int ->
                if (setting.language != locales[i]) {
                    setting.language = locales[i]
                    setLocale(locales[i])
                    dialogInterface.dismiss()
                    setting.isFromRecreate = true
                    parent.recreate()
                }
            }
            .show()
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
            Log.e("test", e.localizedMessage?:"error")
        }
    }

    private fun setLocale(locale: String) {
        resources.apply {
            configuration.setLocale(Locale(locale))
            updateConfiguration(configuration, displayMetrics)
        }
    }

}