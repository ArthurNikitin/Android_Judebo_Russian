package com.byte4b.judebo.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.utils.Setting
import kotlinx.android.synthetic.main.fragment_setting.*
import java.util.*

class SettingFragment : Fragment(R.layout.fragment_setting) {

    private val setting by lazy { Setting(requireActivity()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (setting.language) {
            "ru" -> {
                lang_tv.text = "Русский"
                lang_tv
                    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ru, 0, 0, 0)
            }
            else -> {
                lang_tv.text = "English"
                lang_tv
                    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.en, 0, 0, 0)
            }
        }

        lang_tv.setOnClickListener {
            val languages = arrayOf("English", "Русский")
            val locales = arrayOf("en", "ru")

            AlertDialog.Builder(requireContext())
                .setTitle("Choose language")
                .setItems(languages) { dialogInterface: DialogInterface, i: Int ->
                    if (setting.language != locales[i]) {
                        setting.language = locales[i]
                        setLocale(locales[i])
                        dialogInterface.dismiss()

                    }
                }
                .show()
        }
    }

    private fun setLocale(locale: String) {
        resources.configuration.locale = Locale(locale)
        resources.updateConfiguration(
            resources.configuration,
            resources.displayMetrics
        )
    }

}