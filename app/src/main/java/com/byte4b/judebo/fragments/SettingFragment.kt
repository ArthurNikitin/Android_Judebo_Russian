package com.byte4b.judebo.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.utils.Setting
import kotlinx.android.synthetic.main.fragment_setting.*

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
    }

}