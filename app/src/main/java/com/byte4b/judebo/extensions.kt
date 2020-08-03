package com.byte4b.judebo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
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

fun Context.isHavePermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
fun Context.getLocation(): Location? {
    return try {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    } catch (e: Exception) {
        null
    }
}

fun TextView.setLeftDrawable(@DrawableRes drawable: Int) {
    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(Setting.CURRENCY_ICON_SIZE, Setting.CURRENCY_ICON_SIZE)
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

fun TextView.setRightDrawable(@DrawableRes drawable: Int) {
    val drawable = resources
        .getDrawable( drawable)
        .toBitmap(Setting.CURRENCY_ICON_SIZE, Setting.CURRENCY_ICON_SIZE)
        .toDrawable(resources)
    setCompoundDrawablesWithIntrinsicBounds(if (isRtl) drawable else null ,null, if (!isRtl) drawable else null, null)
}

fun String.round(): String {
    return try {
        ((toDouble() / 100).roundToInt() * 100).toString()
    } catch (e: Exception) {
        this
    }
}