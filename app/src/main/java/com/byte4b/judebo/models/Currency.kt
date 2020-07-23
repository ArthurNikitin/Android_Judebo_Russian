package com.byte4b.judebo.models

import androidx.annotation.DrawableRes
import com.byte4b.judebo.R

class Currency(
    val id: Int,
    val name: String,
    @DrawableRes val icon: Int,
    val rate: Int
)

val currencies = listOf(
    Currency(1, "USD", R.drawable.iusd, 1),
    Currency(3, "RUB", R.drawable.irub, 1)//fix
)