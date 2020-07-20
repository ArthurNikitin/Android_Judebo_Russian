package com.byte4b.judebo.models

import androidx.annotation.DrawableRes
import com.byte4b.judebo.R

class Language(
    val name: String,
    val native: String,
    @DrawableRes val flag: Int,
    val locale: String,
    val currency: String
) {
    val title get() = "$native ($name)"
}

val languages = listOf(
    Language("English", "English", R.drawable.en, "en", "USD"),
    Language("Russian", "Русский", R.drawable.ru, "ru", "RUB")
)