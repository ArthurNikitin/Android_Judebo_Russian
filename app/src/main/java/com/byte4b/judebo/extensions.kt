package com.byte4b.judebo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import com.byte4b.judebo.models.Language
import com.byte4b.judebo.models.languages
import com.byte4b.judebo.utils.Setting
import java.util.*
import kotlin.math.roundToInt


fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, duration).show()

fun Context.toast(message: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

inline fun <reified A : Activity> Context.startActivity(configIntent: Intent.() -> Unit = {}) {
    startActivity(Intent(this, A::class.java).apply(configIntent))
}

@SuppressLint("MissingPermission")
fun Context.getLocation(): Location? {
    return try {
        val mLocationManager =
            applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        val providers = mLocationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = mLocationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy)
                bestLocation = l
        }
        return bestLocation
    } catch (e: Exception) {
        null
    }
}

fun TextView.setLeftDrawable(@DrawableRes drawable: Int, size: Int = Setting.CURRENCY_ICON_SIZE) {
    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(size, size)
        .toDrawable(resources)
   setCompoundDrawablesWithIntrinsicBounds(if (!isRtl) drawable else null,null, if (isRtl) drawable else null, null)
}

val isRtl get() =
    TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) ==
        ViewCompat.LAYOUT_DIRECTION_RTL

fun isRtl(ctx: Context): Boolean {
    Setting(ctx).apply {
        val locale = if (language.isNullOrEmpty()) "en" else language!!
        return TextUtilsCompat.getLayoutDirectionFromLocale(Locale(locale)) ==
                ViewCompat.LAYOUT_DIRECTION_RTL
    }
}

fun TextView.setRightDrawable(@DrawableRes drawable: Int, size: Int = Setting.CURRENCY_ICON_SIZE) {
    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(size, size)
        .toDrawable(resources)
    setCompoundDrawablesWithIntrinsicBounds(if (isRtl) drawable else null ,null, if (!isRtl) drawable else null, null)
}

fun String.round(): String {
    return try {
        ((toDouble() / 100).roundToInt() * 100).toString().getFormattedSalary()
    } catch (e: Exception) {
        this.getFormattedSalary()
    }
}

fun String.getFormattedSalary(split: String = "\u00A0"): String {
    val list = mutableListOf<String>()

    var tmp = ""
    val reverse = reversed()
    for (index in reverse.indices) {
        if (index % 3 == 0) {
            list.add(tmp)
            tmp = ""
        }
        tmp += reverse[index]
    }
    if (tmp.isNotBlank())
        list.add(tmp)

    return list.joinToString(split).reversed()
}

fun getLangFromLocale(): Language {
    for (lang in languages) {
        if (lang.locale == Locale.getDefault().language)
            return lang
    }
    return languages.first()
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    try {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    } catch (e: Exception) {}
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}
