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
    Currency(1, "USD", R.drawable.iusd, 0),
    Currency(2, "RUB", R.drawable.irub, 0)
)