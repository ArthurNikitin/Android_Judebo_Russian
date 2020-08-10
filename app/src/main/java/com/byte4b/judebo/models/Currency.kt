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

    Currency(1, "USD", R.drawable.currency_usd, 63518),
    Currency(2, "EUR", R.drawable.currency_eur, 53926),
    Currency(4, "GBP", R.drawable.currency_gbp, 48541),
    Currency(5, "CNY", R.drawable.currency_cny, 443909),
    Currency(6, "INR", R.drawable.currency_inr, 4759451),
    Currency(10, "AED", R.drawable.currency_aed, 233298),
    Currency(3, "RUB", R.drawable.currency_rub, 4725157),
    Currency(7, "JPY", R.drawable.currency_jpy, 6722757),
    Currency(8, "IDR", R.drawable.currency_idr, 933728356),
    Currency(12, "KRW", R.drawable.currency_krw, 75866123),
    Currency(13, "TRY", R.drawable.currency_try, 442884),
    Currency(14, "VND", R.drawable.currency_vnd, 1472363923),
    Currency(15, "THB", R.drawable.currency_thb, 1986638),
    Currency(16, "PHP", R.drawable.currency_php, 3123827),
    Currency(17, "PLN", R.drawable.currency_pln, 238071),
    Currency(18, "UAH", R.drawable.currency_uah, 1760482),
    Currency(19, "AZN", R.drawable.currency_azn, 108140),
    Currency(20, "UZS", R.drawable.currency_uzs, 647573347),
    Currency(21, "RON", R.drawable.currency_ron, 260664),
    Currency(22, "NOK", R.drawable.currency_nok, 578496),
    Currency(23, "NPR", R.drawable.currency_npr, 7595239),
    Currency(24, "LKR", R.drawable.currency_lkr, 11794298),
    Currency(25, "HUF", R.drawable.currency_huf, 18557184),
    Currency(26, "CZK", R.drawable.currency_czk, 1415660),
    Currency(27, "SEK", R.drawable.currency_sek, 557597),
    Currency(28, "BGN", R.drawable.currency_bgn, 105046),
    Currency(9, "ILS", R.drawable.currency_ils, 216262),
    Currency(29, "RSD", R.drawable.currency_rsd, 655493),
    Currency(30, "TJS", R.drawable.currency_tjs, 6337897),
    Currency(31, "TMT", R.drawable.currency_tmt, 222950),
    Currency(32, "BYN", R.drawable.currency_byn, 155524),
    Currency(33, "HRK", R.drawable.currency_hrk, 401195),
    Currency(34, "AMD", R.drawable.currency_amd, 30591646),
    Currency(11, "GEL", R.drawable.currency_gel, 196272)

)

