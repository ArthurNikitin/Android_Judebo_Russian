package com.byte4b.judebo.models

import androidx.annotation.DrawableRes
import com.byte4b.judebo.R

class Language(
    val id: Int,
    val name: String,
    val native: String,
    @DrawableRes val flag: Int,
    val locale: String,
    val currency: String
) {
    val title get() = "$native ($name)"
}

val languages = listOf(
    Language(2, "English", "English", R.drawable.en, "en", "USD"),
    Language(1, "Russian", "Русский", R.drawable.ru, "ru", "RUB")
)