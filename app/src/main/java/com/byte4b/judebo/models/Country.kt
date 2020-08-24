package com.byte4b.judebo.models

import androidx.annotation.DrawableRes
import com.byte4b.judebo.R.drawable

class Language(
    val id: Int = 0,
    val name: String = "",
    val native: String = "",
    @DrawableRes val flag: Int = drawable.en,
    val locale: String = "en",
    val currency: String = ""
) {
    val title get() = "$native ($name)"
}

val languages = listOf(
    Language(2, "English", "English", drawable.en, "en", "USD"),
    Language(4, "Chinese", "中文", drawable.zh, "zh", "CNY"),
    Language(5, "Hindi", "हिन्दी", drawable.hi, "hi", "INR"),
    Language(44, "Spanish", "Española", drawable.es, "es", "EUR"),
    Language(34, "Arabian", "عربي", drawable.ar, "ar", "AED"),
    Language(15, "Portuguese", "Português", drawable.pt, "pt", "EUR"),
    Language(3, "French", "Française", drawable.fr, "fr", "EUR"),
    Language(1, "Russian", "Русский", drawable.ru, "ru", "RUB"),
    Language(6, "Japanese", "日本人", drawable.ja, "ja", "JPY"),
    Language(7, "Deutsch", "Deutsch", drawable.de, "de", "EUR"),
    Language(8, "Javanese ", "ꦧꦱꦗꦮ", drawable.jv, "jv", "IDR"),
    Language(10, "Korean", "한국어", drawable.ko, "ko", "KRW"),
    Language(9, "Turkish", "Türk", drawable.tr, "tr", "TRY"),
    Language(11, "Vietnamese", "Tiếng Việt", drawable.vi, "vi", "VND"),
    Language(12, "Italian", "Italiana", drawable.it, "it", "EUR"),
    Language(13, "Thai", "ไทย", drawable.th, "th", "THB"),
    Language(14, "Tagalog", "Wikang Tagalog", drawable.tl, "tl", "PHP"),
    Language(18, "Polish", "Polskie", drawable.pl, "pl", "PLN"),
    Language(16, "Ukrainian", "Українська", drawable.uk, "uk", "UAH"),
    Language(17, "Azerbaijani", "azərbaycan dili", drawable.az, "az", "AZN"),
    Language(18, "Uzbek", "Oʻzbek, Ўзбек, أۇزبېك", drawable.uz, "uz", "UZS"),
    Language(19, "Romanian", "Română", drawable.ro, "ro", "RON"),
    Language(20, "Dutch", "Nederlands", drawable.nl, "nl", "EUR"),
    Language(21, "Norwegian", "Riksmål", drawable.no, "no", "NOK"),
    Language(22, "Nepali ", "नेपाली", drawable.ne, "ne", "NPR"),
    Language(23, "Sinhalese", "සිංහල", drawable.si, "si", "LKR"),
    Language(24, "Hungarian", "Magyar", drawable.hu, "hu", "HUF"),
    Language(25, "Greek", "ελληνικά", drawable.el, "el", "EUR"),
    Language(26, "Czech", "čeština", drawable.cs, "cs", "CZK"),
    Language(27, "Swedish", "Svenska", drawable.sv, "sv", "SEK"),
    Language(28, "Bulgarian", "Български", drawable.bg, "bg", "BGN"),
    Language(29, "Hebrew", "עברי", drawable.he, "he", "ILS"),
    Language(30, "Serbian", "Српски", drawable.sr, "sr", "RSD"),
    Language(31, "Tajik", "Тоҷикистон", drawable.tg, "tg", "TJS"),
    Language(32, "Turkmen", "Turkmen", drawable.tk, "tk", "TMT"),
    Language(33, "Belorussian", "Беларус", drawable.be, "be", "BYN"),
    Language(35, "Croatian", "Hrvatski", drawable.hr, "hr", "HRK"),
    Language(36, "Armenian", "հայերեն", drawable.hy, "hy", "AMD"),
    Language(37, "Finnish", "Suomalainen", drawable.fi, "fi", "EUR"),
    Language(38, "Georgian", "ქართული", drawable.ka, "ka", "GEL"),
    Language(39, "Lithuanian", "Lietuvos", drawable.lt, "lt", "EUR"),
    Language(40, "Norwegian Nynorsk", "Norsk Nynorsk", drawable.nn, "nn", "NOK"),
    Language(41, "Slovenian", "Slovenščina", drawable.sl, "sl", "EUR"),
    Language(42, "Latvian", "Latviešu valoda", drawable.lv, "lv", "EUR"),
    Language(43, "Estonian", "Eesti", drawable.et, "et", "EUR")
)