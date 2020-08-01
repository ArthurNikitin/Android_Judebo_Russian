package com.byte4b.judebo.models

import androidx.annotation.DrawableRes
import com.byte4b.judebo.R.drawable

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
    Language(2, "English", "English", drawable.en, "en", "USD"),
    Language(3, "Chinese", "中文", drawable.zh, "zh", "USD"),
    Language(4, "Hindi", "हिन्दी", drawable.hi, "hi", "USD"),
    Language(5, "Spanish", "Española", drawable.es, "es", "EUR"),
    Language(6, "Arabian", "عربي", drawable.ar, "ar", "EUR"),
    Language(7, "Portuguese", "Português", drawable.pt, "pt", "EUR"),
    Language(8, "French", "Française", drawable.fr, "fr", "EUR"),
    Language(1, "Russian", "Русский", drawable.ru, "ru", "RUB"),
    Language(9, "Japanese", "日本人", drawable.ja, "ja", "EUR"),
    Language(10, "Deutsch", "Deutsch", drawable.de, "de", "EUR"),
    Language(11, "Javanese ", "ꦧꦱꦗꦮ", drawable.jv, "jv", "EUR"),
    Language(12, "Korean", "한국어", drawable.ko, "ko", "EUR"),
    Language(13, "Turkish", "Türk", drawable.tr, "tr", "EUR"),
    Language(14, "Vietnamese", "Tiếng Việt", drawable.vi, "vi", "EUR"),
    Language(15, "Italian", "Italiana", drawable.it, "it", "EUR"),
    Language(16, "Thai", "ไทย", drawable.th, "th", "EUR"),
    Language(17, "Tagalog", "Wikang Tagalog", drawable.tl, "tl", "EUR"),
    Language(18, "Polish", "Polskie", drawable.pl, "pl", "EUR"),
    Language(19, "Ukrainian", "Українська", drawable.uk, "uk", "EUR"),
    Language(20, "Azerbaijani", "azərbaycan dili", drawable.az, "az", "EUR"),
    Language(21, "Uzbek", "Oʻzbek, Ўзбек, أۇزبېك", drawable.uz, "uz", "EUR"),
    Language(22, "Romanian", "Română", drawable.ro, "ro", "EUR"),
    Language(23, "Dutch", "Nederlands", drawable.nl, "nl", "EUR"),
    Language(24, "Norwegian", "Riksmål", drawable.no, "no", "EUR"),
    Language(25, "Nepali ", "नेपाली", drawable.ne, "ne", "EUR"),
    Language(26, "Sinhalese", "සිංහල", drawable.si, "si", "EUR"),
    Language(27, "Hungarian", "Magyar", drawable.hu, "hu", "EUR"),
    Language(28, "Greek", "ελληνικά", drawable.el, "el", "EUR"),
    Language(29, "Czech", "čeština", drawable.cs, "cs", "EUR"),
    Language(30, "Swedish", "Svenska", drawable.sv, "sv", "EUR"),
    Language(31, "Bulgarian", "Български", drawable.bg, "bg", "EUR"),
    Language(32, "Hebrew", "עברי", drawable.he, "he", "EUR"),
    Language(33, "Serbian", "Српски", drawable.sr, "sr", "EUR"),
    Language(34, "Tajik", "Тоҷикистон", drawable.tg, "tg", "EUR"),
    Language(35, "Turkmen", "Turkmen", drawable.tk, "tk", "EUR"),
    Language(36, "Belorussian", "Беларус", drawable.be, "be", "EUR"),
    Language(37, "Croatian", "Hrvatski", drawable.hr, "hr", "EUR"),
    Language(38, "Armenian", "հայերեն", drawable.hy, "hy", "EUR"),
    Language(39, "Finnish", "Suomalainen", drawable.fi, "fi", "EUR"),
    Language(40, "Georgian", "ქართული", drawable.ka, "ka", "EUR"),
    Language(41, "Lithuanian", "Lietuvos", drawable.lt, "lt", "EUR"),
    Language(42, "Norwegian Nynorsk", "Norsk Nynorsk", drawable.nn, "nn", "EUR"),
    Language(43, "Slovenian", "Slovenščina", drawable.sl, "sl", "EUR"),
    Language(44, "Latvian", "Latviešu valoda", drawable.lv, "lv", "EUR"),
    Language(45, "Estonian", "Eesti", drawable.et, "et", "EUR")
)