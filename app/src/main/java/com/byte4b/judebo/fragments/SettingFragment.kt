package com.byte4b.judebo.fragments

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.MainActivity
import com.byte4b.judebo.adapters.CurrencyAdapter
import com.byte4b.judebo.adapters.LanguageAdapter
import com.byte4b.judebo.getLangFromLocale
import com.byte4b.judebo.models.currencies
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.utils.Setting
import kotlinx.android.synthetic.main.fragment_setting.*
import java.util.*

class SettingFragment : Fragment(R.layout.fragment_setting) {

    private val parent by lazy { requireActivity() as MainActivity }
    private val setting by lazy { Setting(requireActivity()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showCurrentCurrency()

        if (setting.language == "") {
            val lang = getLangFromLocale()
            lang_tv.text = lang.title
            langIcon_iv.setImageResource(lang.flag)
        } else {
            val lang = languages.first { it.locale == setting.language }
            lang_tv.text = lang.title
            langIcon_iv.setImageResource(lang.flag)
        }

        if (setting.currency == "") {
            if (setting.language == "") {
                val currentCurrency = currencies.first { it.name == getLangFromLocale().currency }
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

        langClickable.setOnClickListener { showLanguageDialog() }
        currencyClickable.setOnClickListener { showCurrencyDialog() }
        support_tv.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
            emailIntent.data = Uri.parse("mailto: ${getString(R.string.settings_title_support_email)}")
            startActivity(emailIntent)
        }
    }

    private fun showCurrencyDialog() {
        val titles = currencies.map { it.name }

        val currency = if (setting.currency == "") {
            if (setting.language == "")
                currencies.first { it.name == getLangFromLocale().currency }
            else
                currencies.first { it.name == setting.currency }
        } else
            currencies.first { it.name == setting.currency }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_title_currency)
            .setAdapter(CurrencyAdapter(requireContext(), currencies.toTypedArray(), currency.name)) { dialog, index ->
                setting.currency = titles[index]
                showCurrentCurrency()
                dialog.dismiss()
            }
            .show()
    }

    private fun showLanguageDialog() {
        val locales = languages.map { it.locale } //название этих локализаций
        val lang = if (setting.language == "") getLangFromLocale().locale else setting.language!!
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
    }

    private fun setLocale(locale: String) {
        resources.apply {
            configuration.setLocale(Locale(locale))
            updateConfiguration(configuration, displayMetrics)
        }
    }

}